package com.sohu.tv.mq.cloud.task;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.bo.ServerAlarmConfig;
import com.sohu.tv.mq.cloud.bo.ServerInfo;
import com.sohu.tv.mq.cloud.bo.ServerInfoExt;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.ServerAlarmConfigService;
import com.sohu.tv.mq.cloud.service.ServerDataService;
import com.sohu.tv.mq.cloud.util.DateUtil;
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

    private static final String ALARM_TITLE = "MQCloud:服务器预警";

    private static String ALARM_MESSAGE_FORMAT = "%s;%s;%s";

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

    @Scheduled(cron = "13 */6 * * * *")
    @SchedulerLock(name = "serverMachineMonitor", lockAtMostFor = 240000, lockAtLeastFor = 240000)
    public void serverMachineMonitor() {
        long start = System.currentTimeMillis();
        Map<String, ServerAlarmConfig> configMap = getWarnConfigMap();
        if (configMap.isEmpty()) {
            logger.warn("get no result from server_alarm_config!");
            return;
        }
        String fetchDataTime = "";
        // 获取当前服务器状态
        List<ServerInfoExt> serverStatusList = serverDataService.queryAllServer(DateUtil.formatYMDNow());
        // 临时缓存报警信息
        Map<String, List<String>> alarmMap = new HashMap<String, List<String>>();
        for (ServerInfoExt serverInfoExt : serverStatusList) {
            if (!isWarn(serverInfoExt, configMap)) {
                continue;
            }
            List<String> alarmList = alarmMap.get(serverInfoExt.getIp());
            if (alarmList == null) {
                alarmList = new ArrayList<String>();
                alarmMap.put(serverInfoExt.getIp(), alarmList);
            }
            // 处理状态信息
            handleMachineStatus(serverInfoExt, configMap.get(serverInfoExt.getIp()), alarmList);
            // 记录数据采集的时间
            if (fetchDataTime == "" && serverInfoExt.getCdate() != null && serverInfoExt.getCtime() != null) {
                fetchDataTime = formatTime(serverInfoExt.getCdate(), serverInfoExt.getCtime());
            }
        }
        // 处理报警信息
        if (!alarmMap.isEmpty()) {
            handleAlarmMessage(alarmMap, fetchDataTime);
        }
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
        }
        // 保存ip与配置信息的映射关系
        Map<String, ServerAlarmConfig> configMap = new HashMap<String, ServerAlarmConfig>();
        // 获取所有的报警配置
        Result<List<ServerAlarmConfig>> allConfig = serverAlarmConfigService.queryAll();
        if (allConfig.isNotOK()) {
            logger.error("get server alarm config err!");
            return configMap;
        }
        Set<String> ipList = new HashSet<>();
        for (ServerInfo serverInfo : serverList) {
            ipList.add(serverInfo.getIp());
        }
        for (ServerAlarmConfig serverAlarmConfig : allConfig.getResult()) {
            if (ipList.contains(serverAlarmConfig.getIp())) {
                configMap.put(serverAlarmConfig.getIp(), serverAlarmConfig);
            }
        }
        return configMap;
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
            List<String> alarmList) {
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
    private void memoryUsage(ServerInfoExt serverInfoExt, int rate, List<String> alarmList) {
        float total = serverInfoExt.getMtotal();
        float free = serverInfoExt.getMfree();
        float usageRate = (total - free) / total * 100;
        if (rate > 0 && usageRate > rate) {
            alarmList.add(String.format(ALARM_MESSAGE_FORMAT, "内存使用率", decimalFormat.format(usageRate) + SUFFIX,
                    rate + SUFFIX));
        }
    }

    /**
     * cpu负载
     * 
     * @param serverInfoExt
     * @param standard
     * @param alarmList
     */
    private void oneMinuteLoad(ServerInfoExt serverInfoExt, int standard, List<String> alarmList) {
        if (standard > 0 && serverInfoExt.getCload1() > standard) {
            alarmList.add(String.format(ALARM_MESSAGE_FORMAT, "CPU负载(一分钟)", serverInfoExt.getCload1(), standard));
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
    private void tcpStatus(ServerInfoExt serverInfoExt, int connect, int wait, List<String> alarmList) {
        // 连接数
        if (connect > 0 && serverInfoExt.getTuse() > connect) {
            alarmList.add(String.format(ALARM_MESSAGE_FORMAT, "tcp连接数", serverInfoExt.getTuse(), connect));
        }
        // 等待数
        if (wait > 0 && serverInfoExt.getTwait() > wait) {
            alarmList.add(String.format(ALARM_MESSAGE_FORMAT, "tcp等待数", serverInfoExt.getTwait(), wait));
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
    private void ioStatus(ServerInfoExt serverInfoExt, int iops, int iobusy, int ioUsageRate, List<String> alarmList) {
        // 磁盘io速率 交互次数/s
        if (iops > 0 && serverInfoExt.getDiops() > iops) {
            alarmList.add(String.format(ALARM_MESSAGE_FORMAT, "iops", serverInfoExt.getDiops(), iops));
        }
        // 磁盘io带宽使用百分比
        if (iobusy > 0 && serverInfoExt.getDbusy() > iobusy) {
            alarmList.add(
                    String.format(ALARM_MESSAGE_FORMAT, "iobusy", serverInfoExt.getDbusy() + SUFFIX, iobusy + SUFFIX));
        }
        // 磁盘各分区使用率
        String[] dspace = serverInfoExt.getDspace() == null ? new String[0] : serverInfoExt.getDspace().split(",");
        Set<String> set = new HashSet<>();
        for (int i = 0; i < dspace.length; i++) {
            String[] ioUsage = dspace[i].split(":");
            if (set.contains(ioUsage[0])) {
                continue;
            }
            set.add(ioUsage[0]);
            if (ioUsageRate > 0 && Float.parseFloat(ioUsage[1]) > ioUsageRate) {
                alarmList.add(
                        String.format(ALARM_MESSAGE_FORMAT, "磁盘使用率-分区：" + ioUsage[0], ioUsage[1] + SUFFIX,
                                ioUsageRate + SUFFIX));
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
    private void cpuUsage(ServerInfoExt serverInfoExt, int threshold, List<String> alarmList) {
        // cpu使用
        if (threshold > 0 && serverInfoExt.getCuser() > threshold) {
            alarmList.add(String.format(ALARM_MESSAGE_FORMAT, "CPU使用率", serverInfoExt.getCuser() + SUFFIX,
                    threshold + SUFFIX));
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
    private void netStatus(ServerInfoExt serverInfoExt, int netIn, int netOut, List<String> alarmList) {
        // net使用
        if (netIn > 0 && serverInfoExt.getNin() > netIn) {
            alarmList.add(String.format(ALARM_MESSAGE_FORMAT, "入网流量(k/s)", serverInfoExt.getNin(), netIn));
        }
        if (netOut > 0 && serverInfoExt.getNout() > netOut) {
            alarmList.add(String.format(ALARM_MESSAGE_FORMAT, "出网流量(k/s)", serverInfoExt.getNout(), netOut));
        }
    }

    /**
     * 处理报警信息
     * 
     * @param map
     */
    private void handleAlarmMessage(Map<String, List<String>> map, String time) {
        if (map.isEmpty()) {
            return;
        }
        // 是否报警
        boolean flag = false;
        StringBuilder content = new StringBuilder("<table border=1>");
        content.append("<thead>");
        content.append("<tr>");
        content.append("<td>");
        content.append("ip");
        content.append("</td>");
        content.append("<td>");
        content.append("报警类别");
        content.append("</td>");
        content.append("<td>");
        content.append("监控值");
        content.append("</td>");
        content.append("<td>");
        content.append("阈值");
        content.append("</td>");
        content.append("</tr>");
        content.append("</thead>");
        content.append("<tbody>");

        for (String ip : map.keySet()) {
            List<String> alarmList = map.get(ip);
            if (alarmList.isEmpty()) {
                continue;
            }
            flag = true;
            content.append("<tr>");
            content.append("<td rowspan=" + alarmList.size() + ">");
            content.append("<a href='");
            content.append(mqCloudConfigHelper.getServerLink());
            content.append("'>" + ip + "</a>");
            content.append("</td>");
            for (int i = 0; i < alarmList.size(); i++) {
                if (i > 0) {
                    content.append("<tr>");
                }
                // 拆分消息，拼装表格
                String[] msg = alarmList.get(i).split(";");
                content.append("<td>" + msg[0] + "</td>");
                content.append("<td style='color:red'>" + msg[1] + "</td>");
                content.append("<td>" + msg[2] + "</td>");
                if (i > 0) {
                    content.append("</tr>");
                }
            }
            content.append("</tr>");
        }
        content.append("</tbody>");
        content.append("</table>");
        // 有报警信息则发送邮件
        if (flag) {
            sendAlertMessage(content.toString(), time);
        }
    }

    /**
     * 发送报警邮件
     * 
     * @param content
     * @param time
     */
    private void sendAlertMessage(String content, String time) {
        alertService.sendMail(ALARM_TITLE, "数据采集时间：" + time + " 详细如下： " + content);
        logger.info(ALARM_TITLE + content);
    }
}
