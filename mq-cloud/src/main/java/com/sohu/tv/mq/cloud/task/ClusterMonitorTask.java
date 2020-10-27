package com.sohu.tv.mq.cloud.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.NameServer;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;

import net.javacrumbs.shedlock.core.SchedulerLock;

/**
 * 集群实例状态监控
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月11日
 */
public class ClusterMonitorTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String NS_ERROR_TITLE = "NameServer";

    private static final String BROKER_ERROR_TITLE = "Broker";

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

    /**
     * 每6分钟监控一次
     */
    @Scheduled(cron = "45 */6 * * * *")
    @SchedulerLock(name = "nameServerMonitor", lockAtMostFor = 345000, lockAtLeastFor = 345000)
    public void nameServerMonitor() {
        if(clusterService.getAllMQCluster() == null) {
            logger.warn("nameServerMonitor mqcluster is null");
            return;
        }
        logger.info("monitor NameServer start");
        long start = System.currentTimeMillis();
        // 缓存报警信息
        Map<Cluster, List<String>> alarmMap = new HashMap<Cluster, List<String>>();
        for (Cluster mqCluster : clusterService.getAllMQCluster()) {
            List<String> alarmList = alarmMap.get(mqCluster);
            if (alarmList == null) {
                alarmList = new ArrayList<String>();
                alarmMap.put(mqCluster, alarmList);
            }
            monitorNameServer(mqCluster, alarmList);
        }
        if (!alarmMap.isEmpty()) {
            handleAlarmMessage(alarmMap, 0, NS_ERROR_TITLE);
        }
        logger.info("monitor NameServer end! use:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * 每7分钟监控一次
     */
    @Scheduled(cron = "50 */7 * * * *")
    @SchedulerLock(name = "brokerMonitor", lockAtMostFor = 345000, lockAtLeastFor = 345000)
    public void brokerMonitor() {
        if(clusterService.getAllMQCluster() == null) {
            logger.warn("brokerMonitor mqcluster is null");
            return;
        }
        logger.info("monitor broker start");
        long start = System.currentTimeMillis();
        // 缓存报警信息
        Map<Cluster, List<String>> alarmMap = new HashMap<Cluster, List<String>>();
        for (Cluster mqCluster : clusterService.getAllMQCluster()) {
            List<String> alarmList = alarmMap.get(mqCluster);
            if (alarmList == null) {
                alarmList = new ArrayList<String>();
                alarmMap.put(mqCluster, alarmList);
            }
            monitorBroker(mqCluster, alarmList);
        }
        if (!alarmMap.isEmpty()) {
            handleAlarmMessage(alarmMap, 1, BROKER_ERROR_TITLE);
        }
        logger.info("monitor broker end! use:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * ping name server
     * 
     * @param mqCluster
     */
    private void monitorNameServer(Cluster mqCluster, List<String> alarmList) {
        Result<List<NameServer>> nameServerListResult = nameServerService.query(mqCluster.getId());
        if (nameServerListResult.isEmpty()) {
            return;
        }
        List<String> nameServerAddressList = new ArrayList<String>();
        for (NameServer ns : nameServerListResult.getResult()) {
            nameServerAddressList.add(ns.getAddr());
        }
        mqAdminTemplate.execute(new MQAdminCallback<Void>() {
            public Void callback(MQAdminExt mqAdmin) throws Exception {
                for (String addr : nameServerAddressList) {
                    try {
                        mqAdmin.getNameServerConfig(Arrays.asList(addr));
                        nameServerService.update(mqCluster.getId(), addr, CheckStatusEnum.OK);
                    } catch (Exception e) {
                        nameServerService.update(mqCluster.getId(), addr, CheckStatusEnum.FAIL);
                        alarmList.add("ns:" + addr + ";Exception: " + e.getMessage());
                    }
                }
                
                return null;
            }

            public Cluster mqCluster() {
                return mqCluster;
            }

            @Override
            public Void exception(Exception e) throws Exception {
                alarmList.add("Exception: " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * ping Broker
     * 
     * @param mqCluster
     */
    private void monitorBroker(Cluster mqCluster, List<String> alarmList) {
        Result<List<Broker>> brokerListResult = brokerService.query(mqCluster.getId());
        if (brokerListResult.isEmpty()) {
            return;
        }
        mqAdminTemplate.execute(new MQAdminCallback<Void>() {
            public Void callback(MQAdminExt mqAdmin) throws Exception {
                List<Broker> brokerList = brokerListResult.getResult();
                for (Broker broker : brokerList) {
                    try {
                        mqAdmin.fetchBrokerRuntimeStats(broker.getAddr());
                        brokerService.update(mqCluster.getId(), broker.getAddr(), CheckStatusEnum.OK);
                    } catch (Exception e) {
                        brokerService.update(mqCluster.getId(), broker.getAddr(), CheckStatusEnum.FAIL);
                        alarmList.add("bk:" + broker.getAddr() + ";Exception: " + e.getMessage());
                    }
                }
                return null;
            }

            public Cluster mqCluster() {
                return mqCluster;
            }

            @Override
            public Void exception(Exception e) throws Exception {
                alarmList.add("Exception: "+ e.getMessage());
                return null;
            }
        });
    }

    /**
     * 处理报警信息
     * 
     * @param alarmList
     * @param type
     * @param alarmTitle
     */
    private void handleAlarmMessage(Map<Cluster, List<String>> alarmMap, int type, String alarmTitle) {
        if (alarmMap.isEmpty()) {
            return;
        }
        // 是否报警
        boolean flag = false;
        StringBuilder smsBuilder = new StringBuilder();
        StringBuilder content = new StringBuilder("<table border=1>");
        content.append("<thead>");
        content.append("<tr>");
        content.append("<td>");
        content.append("集群");
        content.append("</td>");
        content.append("<td>");
        content.append("异常信息");
        content.append("</td>");
        content.append("</tr>");
        content.append("</thead>");
        content.append("<tbody>");
        
        for (Cluster cluster : alarmMap.keySet()) {
            List<String> alarmList = alarmMap.get(cluster);
            if (alarmList.isEmpty()) {
                continue;
            }
            flag = true;
            content.append("<tr>");
            content.append("<td rowspan=" + alarmList.size() + ">");
            content.append("<a href='");
            if (type == 0) {//ns
                content.append(mqCloudConfigHelper.getNameServerMonitorLink(cluster.getId()));
            } else { //broker
                content.append(mqCloudConfigHelper.getBrokerMonitorLink(cluster.getId())); 
            }
            smsBuilder.append("cluster:");
            smsBuilder.append(cluster.getName());
            smsBuilder.append(":");
            content.append("'>" + cluster.getName() + "</a>");
            content.append("</td>");
            for (int i = 0; i < alarmList.size(); i++) {
                if (i > 0) {
                    content.append("<tr>");
                    smsBuilder.append(",");
                }
                String str = alarmList.get(i);
                content.append("<td>" + str + "</td>");
                smsBuilder.append(str.split(";")[0]);
                if (i > 0) {
                    content.append("</tr>");
                }
            }
            smsBuilder.append(";");
            content.append("</tr>");
        }
        content.append("</tbody>");
        content.append("</table>");
        if (flag) {
            sendAlertMessage(alarmTitle, content.toString());
            alertService.sendPhone(alarmTitle, smsBuilder.toString());
        }
    }

    /**
     * 发送报警邮件
     * 
     * @param titile
     * @param content
     */
    private void sendAlertMessage(String title, String content) {
        alertService.sendWarnMail(null, title, content);
        logger.error(title + content);
    }
}
