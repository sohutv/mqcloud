package com.sohu.tv.mq.rocketmq;

import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * lmq pull消费者
 *
 * @date 2026年03月18日
 */
public class RocketMQLmqPullConsumer extends RocketMQPullConsumer {

    private String liteTopic;

    private ScheduledExecutorService liteTopicRouteScheduledExecutorService;

    public RocketMQLmqPullConsumer(String consumerGroup, String parentTopic, String liteTopic) {
        super(CommonUtil.buildLmqConsumer(consumerGroup), parentTopic);
        this.liteTopic = CommonUtil.buildLmqTopic(parentTopic, liteTopic);
    }

    public String getTopic() {
        return liteTopic;
    }

    public Set<MessageQueue> fetchSubscribeMessageQueues() {
        this.isRunning();
        Set<MessageQueue> messageQueues = getDefaultMQPullConsumerImpl().getRebalanceImpl().getTopicSubscribeInfoTable().get(getTopic());
        if (messageQueues == null) {
            return Collections.emptySet();
        }
        Set<MessageQueue> mqs = new HashSet<>();
        for (MessageQueue messageQueue : messageQueues) {
            MessageQueue mq = new MessageQueue(getTopic(), messageQueue.getBrokerName(), (int) MixAll.LMQ_QUEUE_ID);
            mqs.add(mq);
        }
        return mqs;
    }

    @Override
    public void initAfterStart() throws MQClientException {
        // 初始化lmq的topic路由
        initLmqTopicRoute();
        super.initAfterStart();
    }

    /**
     * 初始化lmq的topic路由
     */
    public void initLmqTopicRoute() {
        // 更新MQClientInstance.brokerAddrTable及把父topic加入自动更新路由
        try {
            mqClientInstance.updateTopicRouteInfoFromNameServer(topic);
        } catch (Exception e) {
            logger.error("initLmqTopicRoute:{} updateTopicRouteInfoFromNameServer err", topic, e);
        }
        registerTopics(liteTopic);
        updateLmqTopicRoute();
        // rebalance一次让消息拉取立马生效
        try {
            getDefaultMQPullConsumerImpl().doRebalance();
        } catch (Exception e) {
            logger.error("initLmqTopicRoute:{} doRebalance err", topic, e);
        }
        // 启动定时更新任务
        initLmqTopicTask();
    }

    public void registerTopics(String topic) {
        if (!topic.equals(this.liteTopic)) {
            return;
        }
        super.registerTopics(liteTopic);
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
            TopicRouteData topicRouteData = mqClientInstance.getAnExistTopicRouteData(topic);
            if (topicRouteData == null) {
                logger.warn("updateLmqTopicRoute:{} topicRouteData is null", topic);
                return;
            }
            TopicRouteData prevTopicRouteData = mqClientInstance.getTopicRouteTable().put(liteTopic, topicRouteData);
            if (!Objects.equals(prevTopicRouteData, topicRouteData)) {
                logger.info("updateLmqTopicRoute:{} topicRouteData changed, old:{}, new:{}", liteTopic, prevTopicRouteData, topicRouteData);
            }
            Set<MessageQueue> mqs = new HashSet<>();
            for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
                MessageQueue mq = new MessageQueue(liteTopic, brokerData.getBrokerName(), (int) MixAll.LMQ_QUEUE_ID);
                mqs.add(mq);
            }
            getDefaultMQPullConsumerImpl().subscriptionAutomatically(liteTopic);
            getDefaultMQPullConsumerImpl().updateTopicSubscribeInfo(liteTopic, mqs);
        } catch (Exception e) {
            logger.warn("updateLmqTopicRoute:{} err", liteTopic, e);
        }
    }
}
