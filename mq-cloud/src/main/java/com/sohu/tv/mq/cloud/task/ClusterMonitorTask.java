package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 集群实例状态监控
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月11日
 */
public class ClusterMonitorTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private NameServerService nameServerService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private ControllerService controllerService;

    @Autowired
    private ProxyService proxyService;

    /**
     * 每6分钟监控一次
     */
    @Scheduled(cron = "45 */6 * * * *")
    @SchedulerLock(name = "nameServerMonitor", lockAtMostFor = 345000, lockAtLeastFor = 345000)
    public void nameServerMonitor() {
        if (clusterService.getAllMQCluster() == null) {
            logger.warn("nameServerMonitor mqcluster is null");
            return;
        }
        long start = System.currentTimeMillis();
        List<ClusterStat> clusterStatList = new ArrayList<>();
        for (Cluster mqCluster : clusterService.getAllMQCluster()) {
            ClusterStat clusterStat = monitorNameServer(mqCluster);
            if (clusterStat != null) {
                clusterStatList.add(clusterStat);
            }
        }
        handleAlarmMessage(clusterStatList, WarnType.NAMESERVER_ERROR);
        logger.info("monitor NameServer end! use:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * 每5分钟监控一次
     */
    @Scheduled(cron = "50 */5 * * * *")
    @SchedulerLock(name = "brokerMonitor", lockAtMostFor = 240000, lockAtLeastFor = 240000)
    public void brokerMonitor() {
        List<Cluster> clusters = clusterService.queryAll().getResult();
        if (CollectionUtils.isEmpty(clusters)) {
            logger.warn("brokerMonitor mqcluster is null");
            return;
        }
        long start = System.currentTimeMillis();
        // 缓存broker状态信息
        Map<Cluster, List<Broker>> clusterMap = new HashMap<>();
        List<ClusterStat> clusterStatList = new ArrayList<>();
        for (Cluster cluster : clusters) {
            // 更新期间不再执行broker监控任务
            if (cluster.isBrokerUpdating()) {
                logger.info("cluster:{} monitor is pausing", cluster.getName());
                continue;
            }
            Result<List<Broker>> brokerListResult = brokerService.query(cluster.getId());
            if (brokerListResult.isEmpty()) {
                continue;
            }
            List<Broker> brokerList = brokerListResult.getResult();
            ClusterStat clusterStat = monitorBroker(cluster, brokerList);
            if (clusterStat != null) {
                clusterStatList.add(clusterStat);
            }
            if (brokerList != null && !brokerList.isEmpty()) {
                clusterMap.put(cluster, brokerList);
            }
        }
        handleAlarmMessage(clusterStatList, WarnType.BROKER_ERROR);
        // broker偏移量预警
        brokerFallBehindWarn(clusterMap);
        logger.info("monitor broker end! use:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * ping name server
     * 
     * @param mqCluster
     */
    private ClusterStat monitorNameServer(Cluster mqCluster) {
        Result<List<NameServer>> nameServerListResult = nameServerService.query(mqCluster.getId());
        if (nameServerListResult.isEmpty()) {
            return null;
        }
        List<String> nameServerAddressList = new ArrayList<String>();
        for (NameServer ns : nameServerListResult.getResult()) {
            nameServerAddressList.add(ns.getAddr());
        }
        List<String> statList = new ArrayList<>();
        for (String addr : nameServerAddressList) {
            Result<?> result = nameServerService.healthCheck(mqCluster, addr);
            if (result.isOK()) {
                nameServerService.update(mqCluster.getId(), addr, CheckStatusEnum.OK);
            } else {
                nameServerService.update(mqCluster.getId(), addr, CheckStatusEnum.FAIL);
                statList.add(result.getMessage());
            }
        }
        if (statList.size() == 0) {
            return null;
        }
        ClusterStat clusterStat = new ClusterStat();
        clusterStat.setClusterLink(mqCloudConfigHelper.getNameServerMonitorLink(mqCluster.getId()));
        clusterStat.setClusterName(mqCluster.getName());
        clusterStat.setStats(statList);
        return clusterStat;
    }

    /**
     * ping Broker
     * 
     * @param mqCluster
     */
    private ClusterStat monitorBroker(Cluster mqCluster, List<Broker> brokerList) {
        List<String> statList = new ArrayList<>();
        mqAdminTemplate.execute(new MQAdminCallback<Void>() {
            public Void callback(MQAdminExt mqAdmin) throws Exception {
                for (Broker broker : brokerList) {
                    try {
                        KVTable kvTable = mqAdmin.fetchBrokerRuntimeStats(broker.getAddr());
                        broker.setMaxOffset(NumberUtils.toLong(kvTable.getTable().get("commitLogMaxOffset")));
                        brokerService.update(mqCluster.getId(), broker.getAddr(), CheckStatusEnum.OK);
                    } catch (Exception e) {
                        brokerService.update(mqCluster.getId(), broker.getAddr(), CheckStatusEnum.FAIL);
                        statList.add(broker.getBrokerName() + ":" + (broker.isMaster() ? "master" : "slave") + ":"
                                + broker.getAddr() + ";Exception: " + e.getMessage());
                    }
                }
                return null;
            }

            public Cluster mqCluster() {
                return mqCluster;
            }

            @Override
            public Void exception(Exception e) throws Exception {
                statList.add("Exception: " + e.getMessage());
                return null;
            }
        });
        if (statList.size() == 0) {
            return null;
        }
        ClusterStat clusterStat = new ClusterStat();
        clusterStat.setClusterLink(mqCloudConfigHelper.getBrokerMonitorLink(mqCluster.getId()));
        clusterStat.setClusterName(mqCluster.getName());
        clusterStat.setStats(statList);
        return clusterStat;
    }

    /**
     * 处理报警信息
     * 
     * @param clusterStatList
     * @param warnType
     */
    private void handleAlarmMessage(List<ClusterStat> clusterStatList, WarnType warnType) {
        if (clusterStatList.isEmpty()) {
            return;
        }
        // 发送并保持邮件预警
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("list", clusterStatList);
        alertService.sendWarn(null, warnType, paramMap);
    }

    /**
     * broker最大offset预警
     * 
     * @param clusterMap
     */
    private void brokerFallBehindWarn(Map<Cluster, List<Broker>> clusterMap) {
        if (clusterMap.isEmpty()) {
            return;
        }
        List<SlaveFallBehind> list = new ArrayList<>();
        for (Cluster cluster : clusterMap.keySet()) {
            List<Broker> brokerList = clusterMap.get(cluster);
            for (Broker broker : brokerList) {
                // slave跳过
                if (!broker.isMaster()) {
                    continue;
                }
                // 获取slave
                Broker slave = findSlave(broker, brokerList);
                // 无从节点或从节点宕机忽略落后情况
                if (slave == null || (slave.getCheckStatus() == CheckStatusEnum.FAIL.getStatus())) {
                    continue;
                }
                // 获取slave落后字节
                long fallBehindOffset = broker.getMaxOffset() - slave.getMaxOffset();
                if (fallBehindOffset <= mqCloudConfigHelper.getSlaveFallBehindSize()) {
                    continue;
                }
                SlaveFallBehind slaveFallBehind = new SlaveFallBehind();
                slaveFallBehind.setClusterName(cluster.getName());
                slaveFallBehind.setBrokerLink(mqCloudConfigHelper.getHrefLink(
                        mqCloudConfigHelper.getBrokerMonitorLink(cluster.getId()), broker.getBrokerName()));
                slaveFallBehind.setFallBehindOffset(fallBehindOffset);
                slaveFallBehind.setSlaveFallBehindSize(mqCloudConfigHelper.getSlaveFallBehindSize());
                list.add(slaveFallBehind);
            }
        }
        if (list.size() <= 0) {
            return;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("list", list);
        alertService.sendWarn(null, WarnType.SLAVE_FALL_BEHIND, paramMap);
    }

    private Broker findSlave(Broker master, List<Broker> brokerList) {
        for (Broker broker : brokerList) {
            if (broker.isMaster()) {
                continue;
            }
            if (master.getBrokerName().equals(broker.getBrokerName())) {
                return broker;
            }
        }
        return null;
    }

    /**
     * 每6分钟监控一次
     */
    @Scheduled(cron = "20 */6 * * * *")
    @SchedulerLock(name = "controllerMonitor", lockAtMostFor = 345000, lockAtLeastFor = 345000)
    public void controllerMonitor() {
        if (clusterService.getAllMQCluster() == null) {
            logger.warn("controllerMonitor mqcluster is null");
            return;
        }
        long start = System.currentTimeMillis();
        List<ClusterStat> clusterStatList = new ArrayList<>();
        for (Cluster mqCluster : clusterService.getAllMQCluster()) {
            ClusterStat clusterStat = monitorController(mqCluster);
            if (clusterStat != null) {
                clusterStatList.add(clusterStat);
            }
        }
        handleAlarmMessage(clusterStatList, WarnType.CONTROLLER_ERROR);
        logger.info("monitor controller end! use:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * ping controller
     *
     * @param mqCluster
     */
    private ClusterStat monitorController(Cluster mqCluster) {
        Result<List<Controller>> listResult = controllerService.query(mqCluster.getId());
        if (listResult.isEmpty()) {
            return null;
        }
        List<String> statList = new ArrayList<>();
        listResult.getResult().stream().forEach(controller -> {
            String addr = controller.getAddr();
            Result<?> result = controllerService.healthCheck(mqCluster, addr);
            if (result.isOK()) {
                controllerService.update(mqCluster.getId(), addr, CheckStatusEnum.OK);
            } else {
                controllerService.update(mqCluster.getId(), addr, CheckStatusEnum.FAIL);
                statList.add(result.getMessage());
            }
        });
        if (statList.size() == 0) {
            return null;
        }
        ClusterStat clusterStat = new ClusterStat();
        clusterStat.setClusterLink(mqCloudConfigHelper.getControllerMonitorLink(mqCluster.getId()));
        clusterStat.setClusterName(mqCluster.getName());
        clusterStat.setStats(statList);
        return clusterStat;
    }

    /**
     * 每6分钟监控一次
     */
    @Scheduled(cron = "03 */6 * * * *")
    @SchedulerLock(name = "proxyMonitor", lockAtMostFor = 345000, lockAtLeastFor = 345000)
    public void proxyMonitor() {
        if (clusterService.getAllMQCluster() == null) {
            logger.warn("proxyMonitor mqcluster is null");
            return;
        }
        long start = System.currentTimeMillis();
        List<ClusterStat> clusterStatList = new ArrayList<>();
        for (Cluster mqCluster : clusterService.getAllMQCluster()) {
            ClusterStat clusterStat = monitorProxy(mqCluster);
            if (clusterStat != null) {
                clusterStatList.add(clusterStat);
            }
        }
        handleAlarmMessage(clusterStatList, WarnType.PROXY_ERROR);
        logger.info("monitor proxy end! use:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * ping proxy
     *
     * @param mqCluster
     */
    private ClusterStat monitorProxy(Cluster mqCluster) {
        Result<List<Proxy>> listResult = proxyService.query(mqCluster.getId());
        if (listResult.isEmpty()) {
            return null;
        }
        List<String> statList = new ArrayList<>();
        listResult.getResult().stream().forEach(proxy -> {
            Result<?> result = proxyService.healthCheck(proxy.getAddr());
            if (result.isOK()) {
                proxyService.update(mqCluster.getId(), proxy.getAddr(), CheckStatusEnum.OK);
            } else {
                proxyService.update(mqCluster.getId(), proxy.getAddr(), CheckStatusEnum.FAIL);
                statList.add(result.getMessage());
            }
        });
        if (statList.size() == 0) {
            return null;
        }
        ClusterStat clusterStat = new ClusterStat();
        clusterStat.setClusterLink(mqCloudConfigHelper.getProxyMonitorLink(mqCluster.getId()));
        clusterStat.setClusterName(mqCluster.getName());
        clusterStat.setStats(statList);
        return clusterStat;
    }
}
