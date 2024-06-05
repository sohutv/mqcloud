package com.sohu.tv.mq.cloud.task;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sohu.tv.mq.cloud.task.server.data.Disk.DiskUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.bo.ServerAlarmConfig;
import com.sohu.tv.mq.cloud.bo.ServerInfo;
import com.sohu.tv.mq.cloud.bo.ServerInfoExt;
import com.sohu.tv.mq.cloud.bo.ServerWarn;
import com.sohu.tv.mq.cloud.bo.ServerWarn.ServerWarnItem;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.ServerAlarmConfigService;
import com.sohu.tv.mq.cloud.service.ServerDataService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;

import net.javacrumbs.shedlock.core.SchedulerLock;

/**
 * 服务器状态监控预警
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月30日
 */
public class ServerWarningTask {
    private static final Logger logger = LoggerFactory.getLogger(ServerWarningTask.class);

    private static String SUFFIX = "%";

    private static DecimalFormat decimalFormat = new DecimalFormat(".0");

    @Autowired
    private ServerDataService serverDataService;

    @Autowired
    private ServerAlarmConfigService serverAlarmConfigService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Scheduled(cron = "20 */6 * * * *")
    @SchedulerLock(name = "serverMachineMonitor", lockAtMostFor = 240000, lockAtLeastFor = 240000)
    public void serverMachineMonitor() {
        long start = System.currentTimeMillis();
        Map<String, ServerAlarmConfig> configMap = getWarnConfigMap();
        if (configMap == null || configMap.isEmpty()) {
            logger.warn("get no result from server_alarm_config or server info!");
            return;
        }
        String fetchDataTime = "";
        // 获取当前服务器状态
        List<ServerInfoExt> serverStatusList = serverDataService.queryAllServer(new Date());
        List<ServerWarn> serverWarnList = new ArrayList<>();
        for (ServerInfoExt serverInfoExt : serverStatusList) {
            if (!isWarn(serverInfoExt, configMap)) {
                continue;
            }
            List<ServerWarnItem> alarmList = new ArrayList<>();
            // 处理状态信息
            handleMachineStatus(serverInfoExt, configMap.get(serverInfoExt.getIp()), alarmList);
            if (alarmList.size() == 0) {
                continue;
            }
            // 记录数据采集的时间
            if (fetchDataTime == "" && serverInfoExt.getCdate() != null && serverInfoExt.getCtime() != null) {
                fetchDataTime = formatTime(serverInfoExt.getCdate(), serverInfoExt.getCtime());
            }
            ServerWarn serverWarn = new ServerWarn();
            serverWarn.setIp(serverInfoExt.getIp());
            serverWarn.setHost(serverInfoExt.getHost());
            serverWarn.setIpLink(mqCloudConfigHelper.getServerLink(serverWarn.getIp()));
            serverWarn.setList(alarmList);
            serverWarnList.add(serverWarn);
        }
        // 处理报警信息
        handleAlarmMessage(serverWarnList, fetchDataTime);
        logger.info("monitorMachineStatus end! use:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * 获取各个机器报警的配置
     * 
     * @return
     */
    private Map<String, ServerAlarmConfig> getWarnConfigMap() {
        List<ServerInfo> serverList = serverDataService.queryAllServerInfo();
        if (serverList.isEmpty()) {
            logger.warn("server info list is empty!");
            return null;
        }
        // 获取所有的报警配置
        Map<String, ServerAlarmConfig> configMap = null;
        Result<List<ServerAlarmConfig>> allConfig = serverAlarmConfigService.queryAll();
        if (allConfig.isNotOK()) {
            logger.error("get server alarm config err!");
            configMap = new HashMap<>();
        } else {
            // 保存ip与配置信息的映射关系
            configMap = allConfig.getResult().stream().collect(Collectors.toMap(ServerAlarmConfig::getIp, config -> config));
        }
        final Map<String, ServerAlarmConfig> map = configMap;
        return serverList.stream().map(serverInfo -> {
            return getServerAlarmConfig(map, serverInfo);
        }).collect(Collectors.toMap(ServerAlarmConfig::getIp, config -> config));
    }

    /**
     * 获取报警配置
     * @param configMap
     * @param serverInfo
     * @return
     */
    private ServerAlarmConfig getServerAlarmConfig(Map<String, ServerAlarmConfig> configMap, ServerInfo serverInfo) {
        ServerAlarmConfig serverAlarmConfig = configMap.get(serverInfo.getIp());
        if (serverAlarmConfig == null) {
            serverAlarmConfig = new ServerAlarmConfig();
            serverAlarmConfig.setIp(serverInfo.getIp());
        }
        // 如果配置为空，则设置默认值
        if (serverAlarmConfig.getLoad1() == 0) {
            serverAlarmConfig.setLoad1(serverInfo.getCpus() * 80);
        }
        if (serverAlarmConfig.getCpuUsageRate() == 0) {
            serverAlarmConfig.setCpuUsageRate(90);
        }
        if (serverAlarmConfig.getConnect() == 0) {
            serverAlarmConfig.setConnect(10000);
        }
        if (serverAlarmConfig.getWait() == 0) {
            serverAlarmConfig.setWait(10000);
        }
        if (serverAlarmConfig.getMemoryUsageRate() == 0) {
            serverAlarmConfig.setMemoryUsageRate(90);
        }
        if (serverAlarmConfig.getNetIn() == 0) {
            serverAlarmConfig.setNetIn(102400);
        }
        if (serverAlarmConfig.getNetOut() == 0) {
            serverAlarmConfig.setNetOut(102400);
        }
        if (serverAlarmConfig.getIops() == 0) {
            serverAlarmConfig.setIops(10000);
        }
        if (serverAlarmConfig.getIobusy() == 0) {
            serverAlarmConfig.setIobusy(60);
        }
        if (serverAlarmConfig.getIoUsageRate() == 0) {
            serverAlarmConfig.setIoUsageRate(80);
        }
        return serverAlarmConfig;
    }

    /**
     * 判断是否监控
     * 
     * @param serverInfoExt
     * @param configMap
     * @return
     */
    private boolean isWarn(ServerInfoExt serverInfoExt, Map<String, ServerAlarmConfig> configMap) {
        boolean isWarn = true;
        ServerAlarmConfig serverAlarmConfig = configMap.get(serverInfoExt.getIp());
        // 不存在报警配置的机器不做监控
        if (serverAlarmConfig == null) {
            isWarn = false;
        }
        return isWarn;
    }

    /**
     * 格式化数据采集时间
     * 
     * @param date
     * @param time
     * @return
     */
    private String formatTime(String date, String time) {
        StringBuilder build = new StringBuilder(date);
        build.append(" ");
        build.append(time);
        build.insert(13, ":");
        return build.toString();
    }

    /**
     * 处理监控信息
     * 
     * @param serverInfoExt
     * @param alarmConfig
     * @param alarmList
     */
    private void handleMachineStatus(ServerInfoExt serverInfoExt, ServerAlarmConfig alarmConfig,
            List<ServerWarnItem> alarmList) {
        // 内存
        memoryUsage(serverInfoExt, alarmConfig.getMemoryUsageRate(), alarmList);
        // cpu负载
        oneMinuteLoad(serverInfoExt, alarmConfig.getLoad1(), alarmList);
        // tcp连接
        tcpStatus(serverInfoExt, alarmConfig.getConnect(), alarmConfig.getWait(), alarmList);
        // io
        ioStatus(serverInfoExt, alarmConfig.getIops(), alarmConfig.getIobusy(), alarmConfig.getIoUsageRate(),
                alarmList);
        // cpu使用率
        cpuUsage(serverInfoExt, alarmConfig.getCpuUsageRate(), alarmList);
        // 网络流量
        netStatus(serverInfoExt, alarmConfig.getNetIn(), alarmConfig.getNetOut(), alarmList);
    }

    /**
     * 内存使用
     * 
     * @param serverInfoExt
     * @param rate
     * @param alarmList
     */
    private void memoryUsage(ServerInfoExt serverInfoExt, int rate, List<ServerWarnItem> alarmList) {
        float total = serverInfoExt.getMtotal();
        float free = serverInfoExt.getMfree();
        float usageRate = (total - free) / total * 100;
        if (rate > 0 && usageRate > rate) {
            alarmList.add(new ServerWarnItem("内存使用率", decimalFormat.format(usageRate) + SUFFIX, rate + SUFFIX));
        }
    }

    /**
     * cpu负载
     * 
     * @param serverInfoExt
     * @param standard
     * @param alarmList
     */
    private void oneMinuteLoad(ServerInfoExt serverInfoExt, int standard, List<ServerWarnItem> alarmList) {
        if (standard > 0 && serverInfoExt.getCload1() > standard) {
            alarmList.add(new ServerWarnItem("CPU负载(一分钟)", String.valueOf(serverInfoExt.getCload1()),
                    String.valueOf(standard)));
        }
    }

    /**
     * tcp连接
     * 
     * @param serverInfoExt
     * @param connect
     * @param wait
     * @param alarmList
     */
    private void tcpStatus(ServerInfoExt serverInfoExt, int connect, int wait, List<ServerWarnItem> alarmList) {
        // 连接数
        if (connect > 0 && serverInfoExt.getTuse() > connect) {
            alarmList.add(new ServerWarnItem("tcp连接数", String.valueOf(serverInfoExt.getTuse()),
                    String.valueOf(connect)));
        }
        // 等待数
        if (wait > 0 && serverInfoExt.getTwait() > wait) {
            alarmList.add(new ServerWarnItem("tcp等待数", String.valueOf(serverInfoExt.getTwait()),
                    String.valueOf(wait)));
        }
    }

    /**
     * io状态
     * 
     * @param serverInfoExt
     * @param iops
     * @param iobusy
     * @param alarmList
     */
    private void ioStatus(ServerInfoExt serverInfoExt, int iops, int iobusy, int ioUsageRate,
            List<ServerWarnItem> alarmList) {
        // 磁盘io速率 交互次数/s
        if (iops > 0 && serverInfoExt.getDiops() > iops) {
            alarmList.add(new ServerWarnItem("iops", String.valueOf(serverInfoExt.getDiops()), String.valueOf(iops)));
        }
        // 磁盘io带宽使用百分比
        if (iobusy > 0 && serverInfoExt.getDbusy() > iobusy) {
            alarmList.add(new ServerWarnItem("iobusy", serverInfoExt.getDbusy() + SUFFIX, iobusy + SUFFIX));
        }
        // 磁盘各分区使用率
        if (ioUsageRate <= 0) {
            return;
        }
        List<DiskUsage> diskUsages = serverInfoExt.getDiskUsage();
        if (diskUsages == null || diskUsages.isEmpty()) {
            return;
        }
        Set<String> set = new HashSet<>();
        for (DiskUsage diskUsage : diskUsages) {
            if (set.contains(diskUsage.getMount())) {
                continue;
            }
            set.add(diskUsage.getMount());
            if (diskUsage.getValue() > ioUsageRate) {
                alarmList.add(new ServerWarnItem("磁盘使用率-分区：" + diskUsage.getMount(), diskUsage.getValue() + SUFFIX, ioUsageRate + SUFFIX));
            }
        }
    }

    /**
     * cpu状态
     * 
     * @param serverInfoExt
     * @param threshold
     * @param alarmList
     */
    private void cpuUsage(ServerInfoExt serverInfoExt, int threshold, List<ServerWarnItem> alarmList) {
        // cpu使用
        if (threshold > 0 && serverInfoExt.getCuser() > threshold) {
            alarmList.add(new ServerWarnItem("CPU使用率", serverInfoExt.getCuser() + SUFFIX, threshold + SUFFIX));
        }
    }

    /**
     * 网络状态
     * 
     * @param serverInfoExt
     * @param netIn
     * @param netOut
     * @param alarmList
     */
    private void netStatus(ServerInfoExt serverInfoExt, int netIn, int netOut, List<ServerWarnItem> alarmList) {
        // net使用
        if (netIn > 0 && serverInfoExt.getNin() > netIn) {
            alarmList.add(
                    new ServerWarnItem("入网流量(k/s)", String.valueOf(serverInfoExt.getNin()), String.valueOf(netIn)));
        }
        if (netOut > 0 && serverInfoExt.getNout() > netOut) {
            alarmList.add(
                    new ServerWarnItem("出网流量(k/s)", String.valueOf(serverInfoExt.getNout()), String.valueOf(netOut)));
        }
    }

    /**
     * 处理报警信息
     * 
     * @param map
     */
    private void handleAlarmMessage(List<ServerWarn> serverWarnList, String time) {
        if (serverWarnList.isEmpty()) {
            return;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("time", time);
        paramMap.put("list", serverWarnList);
        alertService.sendWarn(null, WarnType.SERVER_WARN, paramMap);
    }
}
