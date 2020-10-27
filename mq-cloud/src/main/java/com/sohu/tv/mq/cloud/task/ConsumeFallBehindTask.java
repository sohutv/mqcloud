package com.sohu.tv.mq.cloud.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.common.model.BrokerMomentStatsData;
import com.sohu.tv.mq.cloud.common.model.BrokerMomentStatsItem;
import com.sohu.tv.mq.cloud.service.AlarmConfigBridingService;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ConsumeFallBehindService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;

import net.javacrumbs.shedlock.core.SchedulerLock;

/**
 * 消费落后检测任务
 * 
 * @author yongfeigao
 * @date 2020年7月10日
 */
public class ConsumeFallBehindTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private ConsumeFallBehindService consumeFallBehindService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @Autowired
    private AlarmConfigBridingService alarmConfigBridingService;

    /**
     * 检测是否落后太多
     */
    @Scheduled(cron = "13 */6 * * * *")
    @SchedulerLock(name = "detectFallBehindTooMany", lockAtMostFor = 180000, lockAtLeastFor = 170000)
    public void detectFallBehindTooMany() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                long start = System.currentTimeMillis();
                Result<List<Broker>> brokerListResult = brokerService.queryAll();
                if (brokerListResult.isEmpty()) {
                    logger.warn("brokerListResult is empty");
                    return;
                }
                int size = detectFallBehindTooMany(brokerListResult.getResult());
                logger.info("detectFallBehindTooMany size:{}, use:{}ms", size, System.currentTimeMillis() - start);
            }
        });
    }

    /**
     * 检测所有broker
     * 
     * @param brokerList
     */
    private int detectFallBehindTooMany(List<Broker> brokerList) {
        int detectBrokerSize = 0;
        List<Pair<Broker, BrokerMomentStatsData>> fallBehindList = null;
        for (final Broker broker : brokerList) {
            // 非master跳过
            if (broker.getBrokerID() != 0) {
                continue;
            }
            Cluster cluster = clusterService.getMQClusterById(broker.getCid());
            if (cluster == null) {
                logger.warn("cid:{} is no cluster", broker.getCid());
                continue;
            }
            detectBrokerSize++;
            // 获取落后的数据
            Result<BrokerMomentStatsData> result = consumeFallBehindService.getConsumeFallBehindSize(broker.getAddr(),
                    cluster, mqCloudConfigHelper.getConsumeFallBehindSize());
            if (!result.isOK()) {
                continue;
            }
            BrokerMomentStatsData brokerMomentStatsData = result.getResult();
            if (brokerMomentStatsData == null) {
                continue;
            }
            if (fallBehindList == null) {
                fallBehindList = new ArrayList<>();
            }
            fallBehindList.add(new Pair<Broker, BrokerMomentStatsData>(broker, brokerMomentStatsData));
        }
        if (fallBehindList != null) {
            warn(fallBehindList);
        }
        return detectBrokerSize;
    }

    /**
     * 预警
     * 
     * @param fallBehindList
     */
    private void warn(List<Pair<Broker, BrokerMomentStatsData>> fallBehindList) {
        // 过滤掉内置consumer
        for (Pair<Broker, BrokerMomentStatsData> pair : fallBehindList) {
            BrokerMomentStatsData brokerMomentStatsData = pair.getObject2();
            List<BrokerMomentStatsItem> list = brokerMomentStatsData.getBrokerMomentStatsItemList();
            Iterator<BrokerMomentStatsItem> brokerMomentStatsItemIterator = list.iterator();
            while (brokerMomentStatsItemIterator.hasNext()) {
                BrokerMomentStatsItem brokerMomentStatsItem = brokerMomentStatsItemIterator.next();
                String[] split = brokerMomentStatsItem.getKey().split("@");
                // 内置consumer不检测
                if (MixAll.TOOLS_CONSUMER_GROUP.equals(split[2])) {
                    brokerMomentStatsItemIterator.remove();
                }
            }
        }
        // 预警信息拼装
        StringBuilder content = new StringBuilder();
        for (Pair<Broker, BrokerMomentStatsData> pair : fallBehindList) {
            Broker broker = pair.getObject1();
            BrokerMomentStatsData brokerMomentStatsData = pair.getObject2();
            List<BrokerMomentStatsItem> list = brokerMomentStatsData.getBrokerMomentStatsItemList();
            Iterator<BrokerMomentStatsItem> iterator = list.iterator();
            // 过滤掉超频的消费者
            while (iterator.hasNext()) {
                BrokerMomentStatsItem brokerMomentStatsItem = iterator.next();
                String[] split = brokerMomentStatsItem.getKey().split("@");
                if (!alarmConfigBridingService.needWarn("consumerFallBehind", split[1], split[2])) {
                    iterator.remove();
                }
            }
            if (list.size() <= 0) {
                continue;
            }
            content.append("<tr>");
            content.append("<td rowspan=" + list.size() + ">");
            content.append(broker.getBrokerName());
            content.append("</td>");
            content.append("<td rowspan=" + list.size() + ">");
            content.append(broker.getAddr());
            content.append("</td>");
            content.append("<td rowspan=" + list.size() + ">");
            content.append(parseToG(brokerMomentStatsData.getMaxAccessMessageInMemory()));
            content.append("</td>");
            for (int i = 0; i < list.size(); ++i) {
                BrokerMomentStatsItem brokerMomentStatsItem = list.get(i);
                String[] split = brokerMomentStatsItem.getKey().split("@");
                if (i != 0) {
                    content.append("<tr>");
                }
                content.append("<td>");
                content.append(split[1]);
                content.append("</td>");
                content.append("<td>");
                content.append(split[0]);
                content.append("</td>");
                content.append("<td>");
                content.append(mqCloudConfigHelper.getTopicConsumeLink(split[1], split[2]));
                content.append("</td>");
                content.append("<td>");
                content.append(parse(brokerMomentStatsItem.getValue()));
                content.append("</td>");
                content.append("</tr>");
            }
        }
        if (content.length() <= 0) {
            return;
        }
        String header = "<table border=1><thead><tr><th>broker</th><th>地址</th><th>broker内存阈值</th><th>topic</th><th>队列</th>"
                + "<th>consumer</th><th>落后大小</th></tr></thead><tbody>";
        String footer = "</tbody></table>";
        alertService.sendWarnMail(null, "消费落后", header + content.toString() + footer);
    }

    private String parseToG(long bytes) {
        return format((double) bytes / (1024L * 1024L * 1024L)) + "G";
    }

    private String parse(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        }
        if (bytes < 1024L * 1024L) {
            return format((double) bytes / 1024L) + "K";
        }
        if (bytes < 1024L * 1024L * 1024L) {
            return format((double) bytes / (1024L * 1024L)) + "M";
        }
        return parseToG(bytes);
    }

    private float format(double v) {
        return (int)(v * 100) / 100.0f;
    }
}
