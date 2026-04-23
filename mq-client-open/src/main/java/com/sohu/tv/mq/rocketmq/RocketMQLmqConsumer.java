package com.sohu.tv.mq.rocketmq;

import com.sohu.tv.mq.rocketmq.consumer.LmqFilterMessageHook;
import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * lmq push消费者
 *
 * @date 2026年02月10日
 */
public class RocketMQLmqConsumer extends RocketMQConsumer {

    private String liteTopic;

    private ScheduledExecutorService liteTopicRouteScheduledExecutorService;

    public RocketMQLmqConsumer(String consumerGroup, String parentTopic, String liteTopic) {
        super(CommonUtil.buildLmqConsumer(consumerGroup), parentTopic);
        this.liteTopic = CommonUtil.buildLmqTopic(parentTopic, liteTopic);
        consumer.getDefaultMQPushConsumerImpl().registerFilterMessageHook(new LmqFilterMessageHook(this.liteTopic));
    }

    /**
     * 初始化lmq的topic路由
     */
    public void initLmqTopicRoute() throws MQClientException {
        // 更新MQClientInstance.brokerAddrTable及把父topic加入自动更新路由
        try {
            consumer.getDefaultMQPushConsumerImpl().getmQClientFactory().updateTopicRouteInfoFromNameServer(topic);
        } catch (Exception e) {
            logger.error("initLmqTopicRoute:{} updateTopicRouteInfoFromNameServer err", topic, e);
        }
        subscribe(liteTopic);
        updateLmqTopicRoute();
        // rebalance一次让消息拉取立马生效
        try {
            consumer.getDefaultMQPushConsumerImpl().doRebalance();
        } catch (Exception e) {
            logger.error("initLmqTopicRoute:{} doRebalance err", topic, e);
        }
        // 启动定时更新任务
        initLmqTopicTask();
    }

    private void initLmqTopicTask() {
        liteTopicRouteScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "updateLmqTopicRoute-" + getTopic());
            }
        });
        liteTopicRouteScheduledExecutorService.scheduleWithFixedDelay(this::updateLmqTopicRoute, 10, 10, TimeUnit.SECONDS);
    }

    /**
     * 从父topic获取路由，更新lmq的topic路由
     */
    public void updateLmqTopicRoute() {
        try {
            TopicRouteData topicRouteData = getMQClientInstance().getAnExistTopicRouteData(topic);
            if (topicRouteData == null) {
                logger.warn("updateLmqTopicRoute:{} topicRouteData is null", topic);
                return;
            }
            TopicRouteData prevTopicRouteData = consumer.getDefaultMQPushConsumerImpl().getmQClientFactory()
                    .getTopicRouteTable().put(liteTopic, topicRouteData);
            if (!Objects.equals(prevTopicRouteData, topicRouteData)) {
                logger.info("updateLmqTopicRoute:{} topicRouteData changed, old:{}, new:{}", liteTopic, prevTopicRouteData, topicRouteData);
            }
            Set<MessageQueue> mqs = new HashSet<>();
            for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
                MessageQueue mq = new MessageQueue(liteTopic, brokerData.getBrokerName(), (int) MixAll.LMQ_QUEUE_ID);
                mqs.add(mq);
            }
            consumer.getDefaultMQPushConsumerImpl().updateTopicSubscribeInfo(liteTopic, mqs);
        } catch (Exception e) {
            logger.warn("updateLmqTopicRoute:{} err", liteTopic, e);
        }
    }

    @Override
    public String getTopic() {
        return liteTopic;
    }

    @Override
    public void initAfterStart() throws MQClientException {
        // 初始化lmq的topic路由
        initLmqTopicRoute();
        super.initAfterStart();
    }

    @Override
    public void shutdown() {
        liteTopicRouteScheduledExecutorService.shutdown();
        super.shutdown();
    }

    @Override
    public void subscribe(String topic) throws MQClientException {
        if (!topic.equals(this.liteTopic)) {
            return;
        }
        super.subscribe(liteTopic);
    }

    /**
     * queueOffset格式： brokerName:offset
     */
    public void setConsumeFromQueueOffsetWhenBoot(String queueOffset) {
        if (queueOffset == null) {
            return;
        }
        String[] array = queueOffset.split(":");
        if (array.length != 2) {
            logger.warn("setConsumeFromQueueOffsetWhenBoot invalid format:{}", queueOffset);
            return;
        }
        String brokerName = array[0].trim();
        long offset = 0;
        try {
            offset = Long.parseLong(array[1].trim());
            if (offset < 0) {
                logger.warn("setConsumeFromQueueOffsetWhenBoot invalid offset:{}", queueOffset, offset);
                return;
            }
        } catch (NumberFormatException e) {
            logger.warn("setConsumeFromQueueOffsetWhenBoot invalid offset:{}", queueOffset);
            return;
        }
        MessageQueue mq = new MessageQueue(liteTopic, brokerName, (int) MixAll.LMQ_QUEUE_ID);
        Map<MessageQueue, Long> map = new HashMap<>();
        map.put(mq, offset);
        setResetQueueOffsetMap(map);
    }
}
