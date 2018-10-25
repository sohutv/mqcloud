package com.sohu.tv.mq.cloud.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.KVTable;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.ClusterService;
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

    private static final String NS_ERROR_TITLE = "MQCloud:NameServerMonitorErr";

    private static final String BROKER_ERROR_TITLE = "MQCloud:BrokerMonitorErr";

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ClusterService clusterService;

    /**
     * 每6分钟监控一次
     */
    @Scheduled(cron = "45 */6 * * * *")
    @SchedulerLock(name = "nameServerMonitor", lockAtMostFor = 345000, lockAtLeastFor = 345000)
    public void nameServerMonitor() {
        logger.info("monitor NameServer start");
        long start = System.currentTimeMillis();
        // 缓存报警信息
        List<String> alarmList = new ArrayList<String>();
        for (Cluster mqCluster : clusterService.getAllMQCluster()) {
            getStatsFromNameServer(mqCluster, alarmList);
        }
        if (!alarmList.isEmpty()) {
            handleAlarmMessage(alarmList, NS_ERROR_TITLE);
        }
        logger.info("monitor NameServer end! use:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * 每7分钟监控一次
     */
    @Scheduled(cron = "50 */7 * * * *")
    @SchedulerLock(name = "brokerMonitor", lockAtMostFor = 345000, lockAtLeastFor = 345000)
    public void brokerMonitor() {
        logger.info("monitor broker start");
        long start = System.currentTimeMillis();
        // 缓存报警信息
        List<String> alarmList = new ArrayList<String>();
        for (Cluster mqCluster : clusterService.getAllMQCluster()) {
            getBrokerStats(mqCluster, alarmList);
        }
        if (!alarmList.isEmpty()) {
            handleAlarmMessage(alarmList, BROKER_ERROR_TITLE);
        }
        logger.info("monitor broker end! use:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * ping name server
     * 
     * @param mqCluster
     */
    private void getStatsFromNameServer(Cluster mqCluster, List<String> alarmList) {
        mqAdminTemplate.execute(new MQAdminCallback<Void>() {

            @Override
            public Void callback(MQAdminExt mqAdmin) throws Exception {
                List<String> nameServerAddressList = mqAdmin.getNameServerAddressList();
                if (nameServerAddressList.isEmpty()) {
                    alarmList.add("MQCluster:" + mqCluster() + "  the name server address list is empty!");
                    return null;
                }
                Map<String, Properties> nameServerConfig = mqAdmin.getNameServerConfig(nameServerAddressList);
                for (String nsAddr : nameServerAddressList) {
                    Properties properties = nameServerConfig.get(nsAddr);
                    if (properties == null || properties.isEmpty()) {
                        alarmList.add(
                                "MQCluster:" + mqCluster() + " nsAddr:" + nsAddr + " the name server config is empty!");
                    }
                }
                return null;
            }

            @Override
            public Void exception(Exception e) throws Exception {
                alarmList.add("MQCluster:" + mqCluster() + " Exception: " + e);
                return null;
            }

            @Override
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }

    /**
     * ping Broker
     * 
     * @param mqCluster
     */
    private void getBrokerStats(Cluster mqCluster, List<String> alarmList) {
        mqAdminTemplate.execute(new MQAdminCallback<Void>() {

            @Override
            public Void callback(MQAdminExt mqAdmin) throws Exception {
                // 获取集群信息
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                // 获得broker地址map
                HashMap<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
                if (brokerAddrTable.isEmpty()) {
                    alarmList.add("MQCluster:" + mqCluster() + "  the broker address table is empty!");
                    return null;
                }
                // 遍历集群中所有的broker
                for (String brokerName : brokerAddrTable.keySet()) {
                    HashMap<Long, String> brokerAddrs = brokerAddrTable.get(brokerName).getBrokerAddrs();
                    for (Long brokerId : brokerAddrs.keySet()) {
                        KVTable brokerRuntimeStats = mqAdmin.fetchBrokerRuntimeStats(brokerAddrs.get(brokerId));
                        if (brokerRuntimeStats == null || brokerRuntimeStats.getTable().isEmpty()) {
                            alarmList.add("MQCluster:" + mqCluster() + " broker:" + brokerAddrs.get(brokerId)
                                    + " the broker KVTable is empty!");
                        }
                    }
                }
                return null;
            }

            @Override
            public Void exception(Exception e) throws Exception {
                alarmList.add("MQCluster:" + mqCluster() + " Exception: " + e);
                return null;
            }

            @Override
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }

    /**
     * 处理报警信息
     * 
     * @param alarmList
     * @param alarmTitle
     */
    private void handleAlarmMessage(List<String> alarmList, String alarmTitle) {
        StringBuilder content = new StringBuilder("<table>");
        for (String alarmMsg : alarmList) {
            content.append("<br>" + alarmMsg + "</br>");
        }
        content.append("</table>");
        sendAlertMessage(alarmTitle, content.toString());
    }

    /**
     * 发送报警邮件
     * 
     * @param titile
     * @param content
     */
    private void sendAlertMessage(String title, String content) {
        alertService.sendMail(title, "详细如下： " + content);
        logger.error(title + content);
    }
}
