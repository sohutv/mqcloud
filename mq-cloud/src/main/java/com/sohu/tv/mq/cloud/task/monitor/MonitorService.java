package com.sohu.tv.mq.cloud.task.monitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
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
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.admin.TopicOffset;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.Connection;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.common.protocol.topic.OffsetMovedEvent;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.monitor.DeleteMsgsEvent;
import org.apache.rocketmq.tools.monitor.UndoneMsgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.NameServer;
import com.sohu.tv.mq.cloud.bo.TopicConsumer;
import com.sohu.tv.mq.cloud.bo.TypedUndoneMsgs;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.util.Jointer;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.util.CommonUtil;

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

    private DefaultMQAdminExt defaultMQAdminExt;
    private DefaultMQPullConsumer defaultMQPullConsumer;
    private DefaultMQPushConsumer defaultMQPushConsumer;
    
    private String clusterName;
    
    private String nsAddr;
    
    private boolean initialized;
    
    private TopicService topicService;
    
    private ConsumerService consumerService;

    public MonitorService(NameServerService nameServerService, Cluster mqCluster, SohuMonitorListener monitorListener,
            MQCloudConfigHelper mqCloudConfigHelper, TopicService topicService) {
        Result<List<NameServer>> nameServerListResult = nameServerService.query(mqCluster.getId());
        if(nameServerListResult.isEmpty()) {
            logger.error("monitor cluster:{} init err!", mqCluster);
            return;
        }
        this.nsAddr = Jointer.BY_SEMICOLON.join(nameServerListResult.getResult(), ns -> ns.getAddr());
        this.clusterName = mqCluster.getName();
        this.monitorListener = monitorListener;
        this.topicService = topicService;
        
        this.defaultMQPullConsumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP);
        this.defaultMQPullConsumer.setInstanceName(instanceName());
        this.defaultMQPullConsumer.setNamesrvAddr(nsAddr);
        this.defaultMQPullConsumer.setVipChannelEnabled(mqCluster.isEnableVipChannel());
        
        if (mqCloudConfigHelper.isAdminAclEnable()) {
            SessionCredentials adminSessionCredentials = new SessionCredentials(
                    mqCloudConfigHelper.getAdminAccessKey(), mqCloudConfigHelper.getAdminSecretKey());
            this.defaultMQAdminExt = new DefaultMQAdminExt(new AclClientRPCHook(adminSessionCredentials));
        } else {
            this.defaultMQAdminExt = new DefaultMQAdminExt();
        }
        this.defaultMQAdminExt.setVipChannelEnabled(mqCluster.isEnableVipChannel());
        this.defaultMQAdminExt.setInstanceName(instanceName());
        this.defaultMQAdminExt.setNamesrvAddr(nsAddr);
        
        this.defaultMQPushConsumer = new DefaultMQPushConsumer(MixAll.MONITOR_CONSUMER_GROUP);
        this.defaultMQPushConsumer.setVipChannelEnabled(mqCluster.isEnableVipChannel());
        this.defaultMQPushConsumer.setInstanceName(instanceName());
        this.defaultMQPushConsumer.setNamesrvAddr(nsAddr);
        try {
            this.defaultMQPushConsumer.setConsumeThreadMin(1);
            this.defaultMQPushConsumer.setConsumeThreadMax(1);
            this.defaultMQPushConsumer.subscribe(MixAll.OFFSET_MOVED_EVENT, "*");
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
            logger.error("consume topic:{}", MixAll.OFFSET_MOVED_EVENT, e);
        }
        try {
            start();
            initialized = true;
        } catch (MQClientException e) {
            logger.error("start err", e);
            throw new RuntimeException(e);
        }
    }

    private String instanceName() {
        String name = System.currentTimeMillis() + new Random().nextInt() + this.nsAddr;
        return "MonitorService_" + name.hashCode();
    }

    public void start() throws MQClientException {
        this.defaultMQPullConsumer.start();
        this.defaultMQAdminExt.start();
        this.defaultMQPushConsumer.start();
    }

    public void shutdown() {
        this.defaultMQPullConsumer.shutdown();
        this.defaultMQAdminExt.shutdown();
        this.defaultMQPushConsumer.shutdown();
    }

    public void doMonitorWork() throws RemotingException, MQClientException, InterruptedException {
        if(!initialized) {
            logger.warn("doMonitorWork not initialized!");
            return;
        }
        long beginTime = System.currentTimeMillis();
        this.monitorListener.beginRound();
        // 检测集群模式消费者
        TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
        for (String topic : topicList.getTopicList()) {
            if (CommonUtil.isRetryTopic(topic)) {
                String consumerGroup = topic.substring(MixAll.RETRY_GROUP_TOPIC_PREFIX.length());
                // 内置consumer不检测
                if(MixAll.TOOLS_CONSUMER_GROUP.equals(consumerGroup)) {
                    continue;
                }
                // 链接在线检测
                ConsumerConnection cc = getConsumerConnection(consumerGroup);
                if(cc == null) {
                    continue;
                }
                
                try {
                    this.reportUndoneMsgs(consumerGroup, cc);
                } catch (Exception e) {
                    logger.warn("reportUndoneMsgs Exception", e);
                }

                try {
                    this.reportConsumerRunningInfo(consumerGroup, cc);
                } catch (Exception e) {
                    logger.warn("reportConsumerRunningInfo Exception", e);
                }

                try {
                    this.monitorListener.saveConsumerGroupClientInfo(consumerGroup, cc);
                } catch (Exception e) {
                    logger.warn("saveConsumerGroupClientInfo Exception", e);
                }
            }
        }
        this.monitorListener.endRound();
        long spentTimeMills = System.currentTimeMillis() - beginTime;
        logger.info("{} monitor use: {}ms", clusterName, spentTimeMills);
    }
    
    /**
     * 检测广播模式消费者
     * @param broadCastTopicConsumerList
     */
    public void monitorBroadCastConsumer() {
        Result<List<TopicConsumer>> result = topicService.queryTopicConsumer(Consumer.BROADCAST);
        List<TopicConsumer> broadCastTopicConsumerList = result.getResult();
        if (broadCastTopicConsumerList == null) {
            return;
        }
        for (TopicConsumer topicConsumer : broadCastTopicConsumerList) {
            // test不检测
            String tmpConsumer = topicConsumer.getConsumer().toLowerCase();
            if (tmpConsumer.contains("test")) {
                continue;
            }
            try {
                // 链接在线检测
                ConsumerConnection cc = getConsumerConnection(topicConsumer.getConsumer());
                if (cc == null) {
                    continue;
                }
                this.reportUndoneMsgs(topicConsumer.getConsumer(), cc);
            } catch (Exception e) {
                logger.warn("reportUndoneMsgs Exception", e);
            }
        }
    }
    
    private ConsumerConnection getConsumerConnection(String consumerGroup) {
        try {
            return defaultMQAdminExt.examineConsumerConnectionInfo(consumerGroup);
        } catch (Exception e) {
            if(logger.isDebugEnabled()) {
                logger.debug("examineConsumerConnectionInfo consumerGroup:{}, err:{}", consumerGroup, e.getMessage());
            }
        }
        return null;
    }

    private void reportUndoneMsgs(String consumerGroup, ConsumerConnection cc) {
        if(cc.getMessageModel() == MessageModel.CLUSTERING) {
            ConsumeStats cs = null;
            try {
                cs = defaultMQAdminExt.examineConsumeStats(consumerGroup);
            } catch (Exception e) {
                logger.info("examineConsumeStats consumerGroup:{}, err:{}", consumerGroup, e.getMessage());
                return;
            }
            if(cs == null) {
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
                    undoneMsgs.setConsumerGroup(consumerGroup);
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
            TopicStatsTable topicStatsTable = null;
            try {
                topicStatsTable = defaultMQAdminExt.examineTopicStats(topic);
            } catch (Exception e) {
                logger.warn("examineTopicStats topic:{}, err:{}", topic, e.getMessage());
                return;
            }
            if(topicStatsTable == null || topicStatsTable.getOffsetTable().size() == 0) {
                return;
            }
            TypedUndoneMsgs undoneMsgs = new TypedUndoneMsgs();
            undoneMsgs.setTopic(topic);
            undoneMsgs.setConsumerGroup(consumerGroup);
            
            Set<Connection> connSet = cc.getConnectionSet();
            for(Connection conn : connSet) {
                Map<MessageQueue, Long> mqOffsetMap = consumerService.fetchConsumerStatus(defaultMQAdminExt, topic,
                        consumerGroup, conn);
                if (mqOffsetMap == null) {
                    return;
                }
                // 组装数据
                for(MessageQueue mq : mqOffsetMap.keySet()) {
                    TopicOffset topicOffset = topicStatsTable.getOffsetTable().get(mq);
                    // 经过测试，客户端存在broker发生变更，但是消费者状态统计未变更的情况，这样会引起空指针异常
                    if(topicOffset == null) {
                        logger.debug("client:{} topic:{} consumerGroup:{} mq:{} not in topicStatsTable", 
                                conn.getClientId(), topic, consumerGroup, mq);
                        continue;
                    }
                    long undoneMsgsSingleMQ = topicOffset.getMaxOffset() - mqOffsetMap.get(mq);
                    if(undoneMsgsSingleMQ > 0) {
                        undoneMsgs.setUndoneMsgsTotal(undoneMsgs.getUndoneMsgsTotal() + undoneMsgsSingleMQ);
                    }
                    if(undoneMsgsSingleMQ > undoneMsgs.getUndoneMsgsSingleMQ()) {
                        undoneMsgs.setUndoneMsgsSingleMQ(undoneMsgsSingleMQ);
                    }
                }
            }
            this.monitorListener.reportUndoneMsgs(undoneMsgs);
        }
    }

    public void reportConsumerRunningInfo(String consumerGroup, ConsumerConnection cc) throws InterruptedException,
            MQBrokerException, RemotingException, MQClientException {
        TreeMap<String, ConsumerRunningInfo> infoMap = new TreeMap<String, ConsumerRunningInfo>();
        for (Connection c : cc.getConnectionSet()) {
            String clientId = c.getClientId();

            if (c.getVersion() < MQVersion.Version.V3_1_8_SNAPSHOT.ordinal()) {
                continue;
            }

            try {
                ConsumerRunningInfo info = defaultMQAdminExt.getConsumerRunningInfo(consumerGroup, clientId, false);
                infoMap.put(clientId, info);
            } catch (Exception e) {
            }
        }

        if (!infoMap.isEmpty()) {
            this.monitorListener.reportConsumerRunningInfo(infoMap);
        }
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
}
