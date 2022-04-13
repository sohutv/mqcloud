package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.BrokerFallBehind;
import com.sohu.tv.mq.cloud.bo.BrokerFallBehind.ConsumerFallBehind;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.common.model.BrokerMomentStatsData;
import com.sohu.tv.mq.cloud.common.model.BrokerMomentStatsItem;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

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
        List<BrokerFallBehind> brokerFallBehindList = new ArrayList<>();
        for (Pair<Broker, BrokerMomentStatsData> pair : fallBehindList) {
            BrokerMomentStatsData brokerMomentStatsData = pair.getObject2();
            List<BrokerMomentStatsItem> list = brokerMomentStatsData.getBrokerMomentStatsItemList();
            Iterator<BrokerMomentStatsItem> iterator = list.iterator();
            while (iterator.hasNext()) {
                BrokerMomentStatsItem brokerMomentStatsItem = iterator.next();
                String[] split = brokerMomentStatsItem.getKey().split("@");
                // 内置consumer不检测
                if (MixAll.TOOLS_CONSUMER_GROUP.equals(split[2])) {
                    iterator.remove();
                    continue;
                }
                // 过滤掉超频的消费者
                if (!alarmConfigBridingService.needWarn("consumerFallBehind", split[1], split[2])) {
                    iterator.remove();
                }
            }
            if (list.size() <= 0) {
                continue;
            }
            Broker broker = pair.getObject1();
            BrokerFallBehind brokerFallBehind = new BrokerFallBehind();
            brokerFallBehind.setBroker(broker.getBrokerName());
            brokerFallBehind.setAddr(broker.getAddr());
            brokerFallBehind.setMaxAccessMessageInMemory(brokerMomentStatsData.getMaxAccessMessageInMemory());
            brokerFallBehindList.add(brokerFallBehind);
            List<ConsumerFallBehind> consumerFallBehindList = new ArrayList<>();
            for (int i = 0; i < list.size(); ++i) {
                BrokerMomentStatsItem brokerMomentStatsItem = list.get(i);
                String[] split = brokerMomentStatsItem.getKey().split("@");
                ConsumerFallBehind consumerFallBehind = brokerFallBehind.new ConsumerFallBehind();
                consumerFallBehind.setQueue(split[0]);
                consumerFallBehind.setTopic(split[1]);
                consumerFallBehind.setConsumer(split[2]);
                consumerFallBehind.setAccumulated(brokerMomentStatsItem.getValue());
                consumerFallBehind.setConsumerLink(mqCloudConfigHelper.getTopicConsumeHrefLink(split[1], split[2]));
                consumerFallBehindList.add(consumerFallBehind);
            }
            brokerFallBehind.setList(consumerFallBehindList);
        }
        if (brokerFallBehindList.size() == 0) {
            return;
        }
        // 发送并保持邮件预警
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("list", brokerFallBehindList);
        alertService.sendWarn(null, WarnType.CONSUME_FALL_BEHIND, paramMap);
    }
}
