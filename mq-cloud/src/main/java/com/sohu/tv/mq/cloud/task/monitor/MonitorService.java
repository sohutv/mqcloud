package com.sohu.tv.mq.cloud.task.monitor;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.QueueOffset;
import com.sohu.tv.mq.cloud.bo.TypedUndoneMsgs;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.ThreadPoolUtil;
import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.Connection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.topic.OffsetMovedEvent;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.apache.rocketmq.tools.monitor.DeleteMsgsEvent;
import org.apache.rocketmq.tools.monitor.UndoneMsgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * copy from org.apache.rocketmq.tools.monitor.MonitorService
 * 添加多集群支持
 * @Description:
 * @author yongfeigao
 * @date 2018年8月7日
 */
@SuppressWarnings("deprecation")
public class MonitorService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private SohuMonitorListener monitorListener;

    private DefaultMQPullConsumer defaultMQPullConsumer;
    private DefaultMQPushConsumer defaultMQPushConsumer;
    
    private Cluster cluster;
    
    private boolean initialized;
    
    private ConsumerService consumerService;

    private MQAdminTemplate mqAdminTemplate;

    private TopicService topicService;

    private ThreadPoolExecutor monitorThreadPool = ThreadPoolUtil.createBlockingFixedThreadPool("monitor", 2);

    public MonitorService(Cluster mqCluster, SohuMonitorListener monitorListener) {
        this.cluster = mqCluster;
        this.monitorListener = monitorListener;

        this.defaultMQPullConsumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP);
        this.defaultMQPullConsumer.setInstanceName(instanceName());
        this.defaultMQPullConsumer.setUnitName(String.valueOf(mqCluster.getId()));
        this.defaultMQPullConsumer.setVipChannelEnabled(mqCluster.isEnableVipChannel());
        
        this.defaultMQPushConsumer = new DefaultMQPushConsumer(MixAll.MONITOR_CONSUMER_GROUP);
        this.defaultMQPushConsumer.setVipChannelEnabled(mqCluster.isEnableVipChannel());
        this.defaultMQPushConsumer.setInstanceName(instanceName());
        this.defaultMQPushConsumer.setUnitName(String.valueOf(mqCluster.getId()));
        try {
            this.defaultMQPushConsumer.setConsumeThreadMin(1);
            this.defaultMQPushConsumer.setConsumeThreadMax(1);
            this.defaultMQPushConsumer.subscribe(TopicValidator.RMQ_SYS_OFFSET_MOVED_EVENT, "*");
            this.defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
                
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                        ConsumeConcurrentlyContext context) {
                    try {
                        OffsetMovedEvent ome = OffsetMovedEvent.decode(msgs.get(0).getBody(), OffsetMovedEvent.class);
                        
                        DeleteMsgsEvent deleteMsgsEvent = new DeleteMsgsEvent();
                        deleteMsgsEvent.setOffsetMovedEvent(ome);
                        deleteMsgsEvent.setEventTimestamp(msgs.get(0).getStoreTimestamp());
                        
                        MonitorService.this.monitorListener.reportDeleteMsgsEvent(deleteMsgsEvent);
                    } catch (Exception e) {
                    }
                    
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
        } catch (MQClientException e) {
            logger.error("consume topic:{}", TopicValidator.RMQ_SYS_OFFSET_MOVED_EVENT, e);
        }
        try {
            start();
            initialized = true;
        } catch (Exception e) {
            logger.error("start err, cluster:{}", cluster, e);
            throw new RuntimeException(e);
        } finally {
            if (!initialized) {
                shutdown();
            }
        }
    }

    private String instanceName() {
        return "MonitorService_" + System.nanoTime();
    }

    public void start() throws MQClientException {
        this.defaultMQPullConsumer.start();
        this.defaultMQPushConsumer.start();
    }

    public void shutdown() {
        this.defaultMQPullConsumer.shutdown();
        this.defaultMQPushConsumer.shutdown();
    }

    public void doMonitorWork() {
        if(!initialized) {
            logger.warn("doMonitorWork not initialized!");
            return;
        }
        // 获取所有topic
        TopicList topicList = mqAdminTemplate.execute(new DefaultCallback<TopicList>(){
            public TopicList callback(MQAdminExt mqAdmin) throws Exception {
                return mqAdmin.fetchAllTopicList();
            }
            public Cluster mqCluster() {
                return cluster;
            }
        });
        if (topicList == null) {
            return;
        }
        // 检测集群模式消费者
        for (String topic : topicList.getTopicList()) {
            monitorThreadPool.execute(() -> {
                try {
                    doMonitorWork(topic);
                } catch (Throwable e) {
                    logger.error("doMonitorWork err, topic:{}", topic, e);
                }
            });
        }
    }

    private void doMonitorWork(String topic) {
        if (!CommonUtil.isRetryTopic(topic)) {
            return;
        }
        String consumerGroup = topic.substring(MixAll.RETRY_GROUP_TOPIC_PREFIX.length());
        // 内置consumer不检测
        if (MixAll.TOOLS_CONSUMER_GROUP.equals(consumerGroup) || MixAll.MONITOR_CONSUMER_GROUP.equals(consumerGroup)) {
            return;
        }
        // 链接在线检测
        Consumer consumer = consumerService.queryConsumerByName(consumerGroup).getResult();
        if (consumer == null) {
            consumer = new Consumer();
            consumer.setName(consumerGroup);
        }
        ConsumerConnection cc = consumerService.examineConsumerConnectionInfo(consumerGroup, cluster, consumer.isProxyRemoting()).getResult();
        if (cc == null) {
            return;
        }
        // http consumer 监控
        if (consumer.isHttpProtocol()) {
            reportHttpUndoneMsgs(consumer, cc);
        } else {
            try {
                this.reportUndoneMsgs(consumer, cc);
            } catch (Exception e) {
                logger.warn("reportUndoneMsgs Exception", e);
            }

            try {
                this.reportConsumerRunningInfo(consumer, cc);
            } catch (Exception e) {
                logger.warn("reportConsumerRunningInfo Exception", e);
            }
        }
        try {
            this.monitorListener.saveConsumerGroupClientInfo(consumerGroup, cc);
        } catch (Exception e) {
            logger.warn("saveConsumerGroupClientInfo Exception", e);
        }
    }
    
    private void reportUndoneMsgs(Consumer consumer, ConsumerConnection cc) {
        if(cc.getMessageModel() == MessageModel.CLUSTERING) {
            ConsumeStats cs = consumerService.examineConsumeStats(cluster, consumer.getName()).getResult();
            if (cs == null) {
                return;
            }
            HashMap<String/* Topic */, ConsumeStats> csByTopic = new HashMap<String, ConsumeStats>();
            {
                Iterator<Entry<MessageQueue, OffsetWrapper>> it = cs.getOffsetTable().entrySet().iterator();
                while (it.hasNext()) {
                    Entry<MessageQueue, OffsetWrapper> next = it.next();
                    MessageQueue mq = next.getKey();
                    OffsetWrapper ow = next.getValue();
                    ConsumeStats csTmp = csByTopic.get(mq.getTopic());
                    if (null == csTmp) {
                        csTmp = new ConsumeStats();
                        csByTopic.put(mq.getTopic(), csTmp);
                    }
                    csTmp.getOffsetTable().put(mq, ow);
                }
            }
            {
                Iterator<Entry<String, ConsumeStats>> it = csByTopic.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, ConsumeStats> next = it.next();
                    TypedUndoneMsgs undoneMsgs = new TypedUndoneMsgs();
                    undoneMsgs.setConsumerGroup(consumer.getName());
                    undoneMsgs.setTopic(next.getKey());
                    undoneMsgs.setClustering(true);
                    this.computeUndoneMsgs(undoneMsgs, next.getValue());
                    if(undoneMsgs.getUndoneMsgsTotal() <= 0 && undoneMsgs.getUndoneMsgsDelayTimeMills() <= 0) {
                        continue;
                    }
                    this.monitorListener.reportUndoneMsgs(undoneMsgs);
                }
            }
        } else {
            String topic = cc.getSubscriptionTable().keySet().iterator().next();
            TopicStatsTable topicStatsTable = topicService.stats(cluster, topic);
            if(topicStatsTable == null || topicStatsTable.getOffsetTable().size() == 0) {
                return;
            }
            TypedUndoneMsgs undoneMsgs = new TypedUndoneMsgs();
            undoneMsgs.setTopic(topic);
            undoneMsgs.setConsumerGroup(consumer.getName());
            
            Set<Connection> connSet = cc.getConnectionSet();
            for(Connection conn : connSet) {
                Map<MessageQueue, Long> mqOffsetMap = (Map<MessageQueue, Long>) consumerService.fetchConsumerStatus(
                        cluster, topic, consumer.getName(), conn, consumer.isProxyRemoting()).getResult();
                if (mqOffsetMap == null) {
                    return;
                }
                // 组装数据
                for(MessageQueue mq : mqOffsetMap.keySet()) {
                    addUndoneMsgsSingleMQ(undoneMsgs, topicStatsTable, mq, mqOffsetMap.get(mq));
                }
            }
            this.monitorListener.reportUndoneMsgs(undoneMsgs);
        }
    }

    public void reportConsumerRunningInfo(Consumer consumer, ConsumerConnection cc) throws InterruptedException,
            MQBrokerException, RemotingException, MQClientException {
        TreeMap<String, ConsumerRunningInfo> infoMap = new TreeMap<String, ConsumerRunningInfo>();
        for (Connection c : cc.getConnectionSet()) {
            String clientId = c.getClientId();

            if (c.getVersion() < MQVersion.Version.V3_1_8_SNAPSHOT.ordinal()) {
                continue;
            }

            ConsumerRunningInfo info = consumerService.getConsumerRunningInfo(cluster, consumer.getName(), clientId,
                    consumer.isProxyRemoting()).getResult();
            if (info != null) {
                infoMap.put(clientId, info);
            }
        }

        if (!infoMap.isEmpty()) {
            this.monitorListener.reportConsumerRunningInfo(consumer.getName(), infoMap);
        }
    }

    /**
     * http消费模式堆积检测
     * @param consumer
     * @param cc
     */
    private void reportHttpUndoneMsgs(Consumer consumer, ConsumerConnection cc) {
        String topic = cc.getSubscriptionTable().keySet().iterator().next();
        TopicStatsTable topicStatsTable = topicService.stats(cluster, topic);
        if (topicStatsTable == null || topicStatsTable.getOffsetTable().size() == 0) {
            return;
        }
        TypedUndoneMsgs undoneMsgs = new TypedUndoneMsgs();
        undoneMsgs.setTopic(topic);
        undoneMsgs.setConsumerGroup(consumer.getName());

        // 集群模式数据解析
        if (consumer.isClustering()) {
            Result<List<QueueOffset>> result = consumerService.fetchHttpClusteringQueueOffset(consumer.getName());
            if (result.isNotOK()) {
                logger.warn("fetchHttpClusteringQueueOffset:{} error:{}", consumer.getName(), result);
                return;
            }
            List<QueueOffset> queueOffsets = result.getResult();
            for (QueueOffset qo : queueOffsets) {
                addUndoneMsgsSingleMQ(undoneMsgs, topicStatsTable, qo.getMessageQueue(), qo.getCommittedOffset());
            }
        } else {
            Result<Map<String, List<QueueOffset>>> result =
                    consumerService.fetchHttpBroadcastQueueOffset(consumer.getName());
            if (result.isNotOK()) {
                logger.warn("fetchHttpBroadcastQueueOffset:{} error:{}", consumer.getName(), result);
                return;
            }
            Map<String, List<QueueOffset>> map = result.getResult();
            for (List<QueueOffset> queueOffsets : map.values()) {
                for (QueueOffset qo : queueOffsets) {
                    addUndoneMsgsSingleMQ(undoneMsgs, topicStatsTable, qo.getMessageQueue(), qo.getCommittedOffset());
                }
            }
        }
        this.monitorListener.reportUndoneMsgs(undoneMsgs);
    }

    /**
     * 添加单个队列堆积消息量
     * @param undoneMsgs
     * @param topicStatsTable
     * @param mq
     * @param offset
     */
    private void addUndoneMsgsSingleMQ(TypedUndoneMsgs undoneMsgs, TopicStatsTable topicStatsTable, MessageQueue mq,
                               long offset) {
        // offset非法，不保存
        if (offset < 0) {
            return;
        }
        TopicOffset topicOffset = topicStatsTable.getOffsetTable().get(mq);
        if (topicOffset == null) {
            return;
        }
        undoneMsgs.addUndoneMsgsSingleMQ(topicOffset.getMaxOffset() - offset);
    }

    private void computeUndoneMsgs(final UndoneMsgs undoneMsgs, final ConsumeStats consumeStats) {
        long total = 0;
        long singleMax = 0;
        long delayMax = 0;
        Iterator<Entry<MessageQueue, OffsetWrapper>> it = consumeStats.getOffsetTable().entrySet().iterator();
        while (it.hasNext()) {
            Entry<MessageQueue, OffsetWrapper> next = it.next();
            MessageQueue mq = next.getKey();
            OffsetWrapper ow = next.getValue();
            long diff = ow.getBrokerOffset() - ow.getConsumerOffset();

            if (diff > singleMax) {
                singleMax = diff;
            }

            if (diff > 0) {
                total += diff;
            }

            // Delay
            if (ow.getLastTimestamp() > 0) {
                try {
                    long maxOffset = this.defaultMQPullConsumer.maxOffset(mq);
                    if (maxOffset > 0) {
                        PullResult pull = this.defaultMQPullConsumer.pull(mq, "*", maxOffset - 1, 1);
                        switch (pull.getPullStatus()) {
                            case FOUND:
                                long delay = pull.getMsgFoundList().get(0).getStoreTimestamp() - ow.getLastTimestamp();
                                if (delay > delayMax) {
                                    delayMax = delay;
                                }
                                break;
                            case NO_MATCHED_MSG:
                            case NO_NEW_MSG:
                            case OFFSET_ILLEGAL:
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        undoneMsgs.setUndoneMsgsTotal(total);
        undoneMsgs.setUndoneMsgsSingleMQ(singleMax);
        undoneMsgs.setUndoneMsgsDelayTimeMills(delayMax);
    }

    public void setConsumerService(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    public void setMqAdminTemplate(MQAdminTemplate mqAdminTemplate) {
        this.mqAdminTemplate = mqAdminTemplate;
    }

    public void setTopicService(TopicService topicService) {
        this.topicService = topicService;
    }
}
