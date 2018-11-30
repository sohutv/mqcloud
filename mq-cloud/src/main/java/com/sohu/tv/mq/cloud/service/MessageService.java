package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.MQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.Connection;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.QueueData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.apache.rocketmq.tools.admin.api.TrackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.DecodedMessage;
import com.sohu.tv.mq.cloud.bo.MQOffset;
import com.sohu.tv.mq.cloud.bo.MessageData;
import com.sohu.tv.mq.cloud.bo.MessageQueryCondition;
import com.sohu.tv.mq.cloud.bo.MessageTrackExt;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.MessageTypeLoader;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.MessageParam;
import com.sohu.tv.mq.serializable.DefaultMessageSerializer;
import com.sohu.tv.mq.serializable.MessageSerializer;

/**
 * 消息服务
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年8月20日
 */
@Service
public class MessageService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String SUB_EXPRESSION = "*";
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private ConsumerService consumerService;
    
    @Autowired
    private MessageTypeLoader messageTypeLoader;
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    private MessageSerializer<Object> messageSerializer = new DefaultMessageSerializer<Object>();
    
    @Autowired
    private TopicService topicService;
    
    /**
     * 根据key查询消息
     * @param cluster
     * @param topic
     * @param key key
     * @param begin 开始时间
     * @param end 结束时间
     * @return
     */
    public Result<List<DecodedMessage>> queryMessageByKey(Cluster cluster, String topic, String key, long begin, long end) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<List<DecodedMessage>>>() {
            public Result<List<DecodedMessage>> callback(MQAdminExt mqAdmin) throws Exception {
                // 其实这里只能查询64条消息，broker端有限制
                QueryResult queryResult = mqAdmin.queryMessage(topic, key, 100, begin, end);
                List<MessageExt> messageExtList = queryResult.getMessageList();
                if(messageExtList == null || messageExtList.size() == 0) {
                    logger.warn("msg list is null, cluster:{} topic:{} msgId:{}", cluster, topic, key);
                    return Result.getResult(Status.NO_RESULT);
                }
                // 特定类型使用自定义的classloader
                if(mqCloudConfigHelper.getClassList() != null && 
                        mqCloudConfigHelper.getClassList().contains(topic)) {
                    Thread.currentThread().setContextClassLoader(messageTypeLoader);
                }
                List<DecodedMessage> messageList = new ArrayList<DecodedMessage>();
                for (MessageExt msg : messageExtList) {
                    byte[] bytes = msg.getBody();
                    if (bytes == null || bytes.length == 0) {
                        logger.warn("MessageExt={}, MessageBody is null", msg);
                        continue;
                    }
                    messageList.add(toDecodedMessage(msg));
                }
                // 按时间排序
                Collections.sort(messageList, new Comparator<DecodedMessage>() {
                    public int compare(DecodedMessage o1, DecodedMessage o2) {
                        return (int)(o1.getBornTimestamp() - o2.getBornTimestamp());
                    }
                });
                return Result.getResult(messageList);
            }
            public Result<List<DecodedMessage>> exception(Exception e) throws Exception {
                logger.error("queryMessage cluster:{} topic:{} key:{}, err:{}", cluster, topic, key, e.getMessage());
                // 此异常代表查无数据，不进行异常提示
                if(e instanceof MQClientException) {
                    if(((MQClientException) e).getResponseCode() == ResponseCode.NO_MESSAGE) {
                        return Result.getOKResult();
                    }
                }
                return Result.getWebErrorResult(e);
            }
            public Cluster mqCluster() {
                return cluster;
            }
        });
    }
    
    /**
     * 根据msgId查询消息
     * @param cluster
     * @param topic
     * @param msgId
     * @return
     */
    public Result<DecodedMessage> queryMessage(Cluster cluster, String topic, String msgId) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<DecodedMessage>>() {
            public Result<DecodedMessage> callback(MQAdminExt mqAdmin) throws Exception {
                MessageExt messageExt = mqAdmin.viewMessage(topic, msgId);
                byte[] bytes = messageExt.getBody();
                if (bytes == null || bytes.length == 0) {
                    logger.warn("MessageExt={}, MessageBody is null", messageExt);
                    return Result.getResult(Status.NO_RESULT);
                }
                // 特定类型使用自定义的classloader
                if(mqCloudConfigHelper.getClassList() != null && 
                        mqCloudConfigHelper.getClassList().contains(topic)) {
                    Thread.currentThread().setContextClassLoader(messageTypeLoader);
                }
                return Result.getResult(toDecodedMessage(messageExt));
            }
            public Result<DecodedMessage> exception(Exception e) throws Exception {
                logger.error("queryMessage cluster:{} topic:{} msgId:{}", cluster, topic, msgId, e);
                return Result.getWebErrorResult(e);
            }
            public Cluster mqCluster() {
                return cluster;
            }
        });
    }
    
    /**
     * 查询消息
     * 
     * @param messageQueryCondition
     * @return
     * @throws MQClientException
     */
    public Result<MessageData> queryMessage(MessageQueryCondition messageQueryCondition) {
        List<DecodedMessage> messageList = null;
        MQPullConsumer consumer = null;
        try {
            // 获取消费者
            Cluster cluster = clusterService.getMQClusterById(messageQueryCondition.getCid());
            consumer = getConsumer(cluster);
            // 初始化参数
            if (messageQueryCondition.getMqOffsetList() == null) {
                List<MQOffset> mqOffsetList = getMQOffsetList(cluster, consumer, messageQueryCondition, true);
                if (mqOffsetList == null) {
                    return Result.getResult(Status.NO_RESULT);
                }
                messageQueryCondition.setMqOffsetList(mqOffsetList);
            }
            // 特定类型使用自定义的classloader
            if(mqCloudConfigHelper.getClassList() != null && 
                    mqCloudConfigHelper.getClassList().contains(messageQueryCondition.getTopic())) {
                Thread.currentThread().setContextClassLoader(messageTypeLoader);
            }
            messageList = new ArrayList<DecodedMessage>();
            for (MQOffset mqOffset : messageQueryCondition.getMqOffsetList()) {
                // 无消息跳过
                if (!mqOffset.hasMessage()) {
                    continue;
                }
                fetchMessage(consumer, messageQueryCondition, mqOffset, messageList);
                if (!messageQueryCondition.needSearch()) {
                    break;
                }
            }
            // 排序
            sort(messageList);
        } catch (Exception e) {
            logger.error("queryMessage", e);
            return Result.getWebErrorResult(e);
        } finally {
            if (consumer != null) {
                try {
                    consumer.shutdown();
                } catch (Exception e) {
                }
            }
        }
        // 计算剩余消息
        messageQueryCondition.calculateLeftSize();
        // 拼装返回对象
        MessageData md = new MessageData();
        md.setMp(messageQueryCondition);
        md.setMsgList(messageList);
        return Result.getResult(md);
    }

    /**
     * 抓取消息
     * 
     * @param consumer
     * @param messageQueryCondition
     * @param mqOffset
     * @param messageList
     * @throws Exception
     */
    private void fetchMessage(MQPullConsumer consumer, MessageQueryCondition messageQueryCondition, MQOffset mqOffset,
            List<DecodedMessage> messageList) throws Exception {
        while (mqOffset.hasMessage() && messageQueryCondition.needSearch()) {
            try {
                // 拉取消息
                PullResult pullResult = consumer.pull(mqOffset.getMq(), SUB_EXPRESSION, mqOffset.getOffset(), 32);
                mqOffset.setOffset(pullResult.getNextBeginOffset());
                // 无消息继续
                if (PullStatus.FOUND != pullResult.getPullStatus()) {
                    continue;
                }
                messageQueryCondition.setSearchedSize(messageQueryCondition.getSearchedSize()
                        + pullResult.getMsgFoundList().size());
                for (MessageExt msg : pullResult.getMsgFoundList()) {
                    // 过滤不在时间范围的消息
                    if (!messageQueryCondition.valid(msg.getStoreTimestamp())) {
                        continue;
                    }
                    byte[] bytes = msg.getBody();
                    if (bytes == null || bytes.length == 0) {
                        logger.warn("MessageExt={}, MessageBody is null", msg);
                        continue;
                    }
                    DecodedMessage m = toDecodedMessage(msg);
                    // 判断是否包含关键字
                    if (messageQueryCondition.getKey() == null) {
                        messageList.add(m);
                    } else {
                        String message = m.getDecodedBody();
                        int start = message.indexOf(messageQueryCondition.getKey());
                        if (start == -1) {
                            continue;
                        }
                        // 关键字加粗显示
                        int keySize = messageQueryCondition.getKey().length();
                        if (start == 0) {
                            message = "<b>" + messageQueryCondition.getKey() + "</b>" + message.substring(keySize);
                        } else {
                            message = message.substring(0, start) + "<b>" + messageQueryCondition.getKey() + "</b>"
                                    + message.substring(start + keySize);
                        }
                        m.setDecodedBody(message);
                        messageList.add(m);
                    }
                }
                // 保存当前记录数
                messageQueryCondition.setCurSize(messageList.size());
            } catch (Exception e) {
                logger.info("fetch message err", e);
                throw e;
            }
        }
    }
    
    /**
     * 转换为解码后的消息
     * @param msg
     * @return DecodedMessage
     */
    private DecodedMessage toDecodedMessage(MessageExt msg) {
        DecodedMessage m = new DecodedMessage();
        byte[] bytes = msg.getBody();
        msg.setBody(null);
        Object decodedBody = bytes;
        try {
            decodedBody = messageSerializer.deserialize(bytes);
        } catch (Exception e) {
            logger.debug("deserialize topic:{} message err:{}", msg.getTopic(), e.getMessage());
            decodedBody = bytes;
        }
        // 将主体消息转换为String
        if (decodedBody instanceof byte[]) {
            m.setDecodedBody(new String((byte[]) decodedBody));
        } else if (decodedBody instanceof String) {
            m.setDecodedBody((String) decodedBody);
        } else if (decodedBody instanceof Map && 
                mqCloudConfigHelper.getMapWithByteList() != null &&
                !mqCloudConfigHelper.getMapWithByteList().contains(msg.getTopic())) {
            m.setDecodedBody(decodedBody.toString());
        } else {
            m.setDecodedBody(JSON.toJSONString(decodedBody));
        }
        BeanUtils.copyProperties(msg, m);
        return m;
    }

    /**
     * 获取mq偏移量数据
     * 
     * @param consumer
     * @param messageQueryCondition
     * @return
     * @throws MQClientException
     */
    private List<MQOffset> getMQOffsetList(Cluster cluster, MQPullConsumer consumer, 
            MessageQueryCondition messageQueryCondition, boolean retryWithErr) {
        List<MQOffset> offsetList = null;
        try {
            Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(messageQueryCondition.getTopic());
            offsetList = new ArrayList<MQOffset>();
            for (MessageQueue mq : mqs) {
                long minOffset = 0;
                long maxOffset = 0;
                try {
                    minOffset = consumer.searchOffset(mq, messageQueryCondition.getStart());
                    maxOffset = consumer.searchOffset(mq, messageQueryCondition.getEnd());
                } catch (Exception e) {
                    logger.warn("mq:{} start:{} end:{} offset err:{}", mq, messageQueryCondition.getStart(),
                            messageQueryCondition.getEnd(),
                            e.getMessage());
                    continue;
                }
                // 拼装offset
                MQOffset mqOffset = new MQOffset();
                mqOffset.setMq(mq);
                mqOffset.setMaxOffset(maxOffset);
                mqOffset.setMinOffset(minOffset);
                mqOffset.setOffset(minOffset);
                offsetList.add(mqOffset);
            }
        } catch (Exception e) {
            if(retryWithErr 
                    && e instanceof MQClientException 
                    && messageQueryCondition.getTopic().startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX)
                    && e.getMessage().contains("Can not find Message Queue for this topic")) {
                TopicRouteData topicRouteData = topicService.route(cluster, messageQueryCondition.getTopic());
                if(topicRouteData != null) {
                    List<QueueData> queueDatas = topicRouteData.getQueueDatas();
                    QueueData queueData = queueDatas.get(0);
                    TopicConfig topicConfig = new TopicConfig();
                    topicConfig.setTopicName(messageQueryCondition.getTopic());
                    topicConfig.setWriteQueueNums(queueData.getWriteQueueNums());
                    topicConfig.setReadQueueNums(queueData.getReadQueueNums());
                    topicConfig.setTopicSysFlag(queueData.getTopicSynFlag());
                    Result<?> result = topicService.createAndUpdateTopicOnCluster(cluster, topicConfig);
                    if(result.isNotOK()) {
                        logger.warn("change topic:{} perm failed, {}", topicConfig.getTopicName(), result);
                    } else {
                        // 尝试一次
                        return getMQOffsetList(cluster, consumer, messageQueryCondition, false);
                    }
                }
            }
            logger.error("getMQOffsetList", e);
        }
        return offsetList;
    }

    /**
     * 获取消费者
     * 
     * @return
     * @throws MQClientException
     */
    private MQPullConsumer getConsumer(Cluster mqCluster) throws MQClientException {
        // 解析ns
        List<String> nsList = mqAdminTemplate.execute(new DefaultCallback<List<String>>() {
            public Cluster mqCluster() {
                return mqCluster;
            }

            public List<String> callback(MQAdminExt mqAdmin) throws Exception {
                return mqAdmin.getNameServerAddressList();
            }
        });
        StringBuilder sb = new StringBuilder();
        for (String str : nsList) {
            sb.append(str);
            sb.append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP, null);
        consumer.setNamesrvAddr(sb.toString());
        consumer.setVipChannelEnabled(mqCluster.isEnableVipChannel());
        consumer.start();
        return consumer;
    }

    /**
     * 按照时间升序排序
     * 
     * @param messageList
     */
    private void sort(List<DecodedMessage> messageList) {
        Collections.sort(messageList, new Comparator<MessageExt>() {
            public int compare(MessageExt o1, MessageExt o2) {
                if (o1.getStoreTimestamp() - o2.getStoreTimestamp() == 0) {
                    return 0;
                }
                return (o1.getStoreTimestamp() > o2.getStoreTimestamp()) ? -1 : 1;
            }
        });
    }

    /**
     * 消息轨迹
     * 
     * @param msg
     * @return
     */
    public Result<?> track(MessageParam mp) {
        // 获取集群
        Cluster mqCluster = clusterService.getMQClusterById(mp.getCid());
        if (mqCluster == null) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        // 获取消费者
        Result<List<Consumer>> consumerListResult = consumerService.queryByTid(mp.getTid());
        if (consumerListResult.isEmpty()) {
            return consumerListResult;
        }
        List<Consumer> consumerList = consumerListResult.getResult();
        // 查询消息轨迹
        Result<?> result = mqAdminTemplate.execute(new MQAdminCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                List<MessageTrack> result = new ArrayList<MessageTrack>();
                for (Consumer consumer : consumerList) {
                    MessageTrack mt = messageTrack(mqAdmin, consumer.getName(), mp, consumer.isBroadcast());
                    result.add(mt);
                }
                return Result.getResult(result);
            }

            public Result<?> exception(Exception e) throws Exception {
                logger.error("cluster:{} topic:{} error", mqCluster, mp.getTopic(), e);
                return Result.getWebErrorResult(e);
            }

            public Cluster mqCluster() {
                return mqCluster;
            }
        });
        return result;
    }

    /**
     * 消息轨迹
     * 
     * @param mqAdmin
     * @param group
     * @param mp
     * @return
     * @throws Exception
     */
    private MessageTrackExt messageTrack(MQAdminExt mqAdmin, String group, MessageParam mp, 
            boolean isBroadcast) throws Exception {
        MessageTrackExt mt = new MessageTrackExt();
        mt.setConsumerGroup(group);
        mt.setTrackType(TrackType.UNKNOWN);
        ConsumerConnection cc = null;
        try {
            cc = mqAdmin.examineConsumerConnectionInfo(group);
        } catch (MQBrokerException e) {
            if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                mt.setTrackType(TrackType.NOT_ONLINE);
            }
            mt.setExceptionDesc("CODE:" + e.getResponseCode() + " DESC:" + e.getErrorMessage());
            return mt;
        } catch (Exception e) {
            mt.setExceptionDesc(RemotingHelper.exceptionSimpleDesc(e));
            return mt;
        }
        
        ClusterInfo ci = mqAdmin.examineBrokerClusterInfo();

        switch (cc.getConsumeType()) {
            case CONSUME_ACTIVELY:
                mt.setTrackType(TrackType.PULL);
                break;
            case CONSUME_PASSIVELY:
                boolean ifConsumed = false;
                try {
                    if(isBroadcast) {
                        ifConsumed = this.consumed(mqAdmin, cc.getConnectionSet(), mp, group, ci.getBrokerAddrTable());
                    } else {
                        ifConsumed = this.consumed(mqAdmin, mp, group, ci.getBrokerAddrTable());
                    }
                } catch (MQClientException e) {
                    if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                        mt.setTrackType(TrackType.NOT_ONLINE);
                    }
                    mt.setExceptionDesc("CODE:" + e.getResponseCode() + " DESC:" + e.getErrorMessage());
                    return mt;
                } catch (MQBrokerException e) {
                    if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                        mt.setTrackType(TrackType.NOT_ONLINE);
                    }
                    mt.setExceptionDesc("CODE:" + e.getResponseCode() + " DESC:" + e.getErrorMessage());
                    return mt;
                } catch (Exception e) {
                    mt.setExceptionDesc(RemotingHelper.exceptionSimpleDesc(e));
                    return mt;
                }

                if (ifConsumed) {
                    mt.setTrackType(TrackType.CONSUMED);
                    Iterator<Entry<String, SubscriptionData>> it = cc.getSubscriptionTable().entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<String, SubscriptionData> next = it.next();
                        if (next.getKey().equals(mp.getTopic())) {
                            if (next.getValue().getTagsSet().contains("*")
                                    || next.getValue().getTagsSet().isEmpty()) {
                            } else {
                                mt.setTrackType(TrackType.CONSUMED_BUT_FILTERED);
                            }
                        }
                    }
                } else {
                    mt.setTrackType(TrackType.NOT_CONSUME_YET);
                }
                break;
            default:
                break;
        }
        return mt;
    }

    /**
     * 是否消费过
     * 
     * @param mqAdmin
     * @param mp
     * @param group
     * @return
     * @throws Exception
     */
    private boolean consumed(MQAdminExt mqAdmin, MessageParam mp, String group, 
            Map<String, BrokerData> brokerAddrTable) throws Exception {
        ConsumeStats consumeStats = mqAdmin.examineConsumeStats(group);
        HashMap<MessageQueue, OffsetWrapper> offsetTable = consumeStats.getOffsetTable();
        Map<MessageQueue, Long> consumerOffsetMap = new HashMap<>();
        for(MessageQueue messageQueue : offsetTable.keySet()) {
            consumerOffsetMap.put(messageQueue, offsetTable.get(messageQueue).getConsumerOffset());
        }
        
        return consumed(consumerOffsetMap, mp, brokerAddrTable);
    }
    
    /**
     * 广播模式，需要判断每个链接
     * @param mqAdmin
     * @param connSet
     * @param messageParam
     * @param consumer
     * @param brokerAddrTable
     * @return
     * @throws Exception
     */
    private boolean consumed(MQAdminExt mqAdmin, Set<Connection> connSet, MessageParam messageParam, String consumer, 
            Map<String, BrokerData> brokerAddrTable) throws Exception {
        for(Connection conn : connSet) {
            // 抓取状态
            Map<String, Map<MessageQueue, Long>> consumerStatusTable = 
                    mqAdmin.getConsumeStatus(messageParam.getTopic(), consumer, conn.getClientId());
            if(consumerStatusTable == null) {
                return false;
            }
            for(Map<MessageQueue, Long> map : consumerStatusTable.values()) {
                if(!consumed(map, messageParam, brokerAddrTable)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 判断是否消费了
     * @param consumerOffsetMap
     * @param messageParam
     * @param brokerAddrTable
     * @return
     */
    private boolean consumed(Map<MessageQueue, Long> consumerOffsetMap, MessageParam messageParam, 
        Map<String, BrokerData> brokerAddrTable) {
        for(MessageQueue messageQueue : consumerOffsetMap.keySet()) {
            // topic不一致
            if(!messageQueue.getTopic().equals(messageParam.getTopic())) {
                continue;
            }
            // queueid不一致
            if(messageQueue.getQueueId() != messageParam.getQueueId()) {
                continue;
            }
            // offset太小
            if(consumerOffsetMap.get(messageQueue) <=  messageParam.getQueueOffset()) {
                continue;
            }
            BrokerData brokerData = brokerAddrTable.get(messageQueue.getBrokerName());
            if(brokerData == null) {
                continue;
            }
            String addr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
            if (addr.equals(messageParam.getStoreHost())) {
                return true;
            }
        }
        return false;
    }
}
