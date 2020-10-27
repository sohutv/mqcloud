package com.sohu.tv.mq.cloud.service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.MQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.trace.TraceBean;
import org.apache.rocketmq.client.trace.TraceContext;
import org.apache.rocketmq.client.trace.TraceType;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.Connection;
import org.apache.rocketmq.common.protocol.body.ConsumeMessageDirectlyResult;
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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.DecodedMessage;
import com.sohu.tv.mq.cloud.bo.DecodedMessage.MessageBodyType;
import com.sohu.tv.mq.cloud.bo.MQOffset;
import com.sohu.tv.mq.cloud.bo.MessageData;
import com.sohu.tv.mq.cloud.bo.MessageQueryCondition;
import com.sohu.tv.mq.cloud.bo.MessageTrackExt;
import com.sohu.tv.mq.cloud.bo.ResentMessageResult;
import com.sohu.tv.mq.cloud.bo.TraceMessageDetail;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper.MQCloudConfigEvent;
import com.sohu.tv.mq.cloud.util.MQCloudIdStrategy;
import com.sohu.tv.mq.cloud.util.MessageDelayLevel;
import com.sohu.tv.mq.cloud.util.MessageTypeClassLoader;
import com.sohu.tv.mq.cloud.util.MsgTraceDecodeUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.MessageParam;
import com.sohu.tv.mq.cloud.web.vo.TraceViewVO;
import com.sohu.tv.mq.cloud.web.vo.TraceViewVO.RequestViewVO;
import com.sohu.tv.mq.serializable.DefaultMessageSerializer;
import com.sohu.tv.mq.serializable.MessageSerializer;
import com.sohu.tv.mq.serializable.MessageSerializerEnum;
import com.sohu.tv.mq.util.CommonUtil;

/**
 * 消息服务
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年8月20日
 */
@Service
@SuppressWarnings("deprecation")
public class MessageService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String SUB_EXPRESSION = "*";

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private ConsumerService consumerService;

    private volatile MessageTypeClassLoader messageTypeClassLoader;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    private MessageSerializer<Object> messageSerializer = new DefaultMessageSerializer<Object>();

    @Autowired
    private TopicService topicService;

    /**
     * 根据key查询消息
     * 
     * @param cluster
     * @param topic
     * @param key key
     * @param begin 开始时间
     * @param end 结束时间
     * @return
     */
    public Result<List<DecodedMessage>> queryMessageByKey(Cluster cluster, String topic, String key, long begin,
            long end) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<List<DecodedMessage>>>() {
            public Result<List<DecodedMessage>> callback(MQAdminExt mqAdmin) throws Exception {
                // 获取broker集群信息
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                // 其实这里只能查询64条消息，broker端有限制
                QueryResult queryResult = mqAdmin.queryMessage(topic, key, 100, begin, end);
                List<MessageExt> messageExtList = queryResult.getMessageList();
                if (messageExtList == null || messageExtList.size() == 0) {
                    logger.warn("msg list is null, cluster:{} topic:{} msgId:{}", cluster, topic, key);
                    return Result.getResult(Status.NO_RESULT);
                }
                // 特定类型使用自定义的classloader
                if (mqCloudConfigHelper.getClassList() != null &&
                        mqCloudConfigHelper.getClassList().contains(topic)) {
                    Thread.currentThread().setContextClassLoader(messageTypeClassLoader);
                }
                List<DecodedMessage> messageList = new ArrayList<DecodedMessage>();
                for (MessageExt msg : messageExtList) {
                    byte[] bytes = msg.getBody();
                    if (bytes == null || bytes.length == 0) {
                        logger.warn("MessageExt={}, MessageBody is null", msg);
                        continue;
                    }
                    messageList.add(toDecodedMessage(msg, clusterInfo));
                }
                // 按时间排序
                Collections.sort(messageList, new Comparator<DecodedMessage>() {
                    public int compare(DecodedMessage o1, DecodedMessage o2) {
                        return (int) (o1.getBornTimestamp() - o2.getBornTimestamp());
                    }
                });
                return Result.getResult(messageList);
            }

            public Result<List<DecodedMessage>> exception(Exception e) throws Exception {
                logger.error("queryMessage cluster:{} topic:{} key:{}, err:{}", cluster, topic, key, e.getMessage());
                // 此异常代表查无数据，不进行异常提示
                if (e instanceof MQClientException) {
                    if (((MQClientException) e).getResponseCode() == ResponseCode.NO_MESSAGE) {
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
     * 
     * @param cluster
     * @param topic
     * @param msgId
     * @return
     */
    public Result<DecodedMessage> queryMessage(Cluster cluster, String topic, String msgId) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<DecodedMessage>>() {
            public Result<DecodedMessage> callback(MQAdminExt mqAdmin) throws Exception {
                // 获取broker集群信息
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                // 获取消息
                MessageExt messageExt = mqAdmin.viewMessage(topic, msgId);
                byte[] bytes = messageExt.getBody();
                if (bytes == null || bytes.length == 0) {
                    logger.warn("MessageExt={}, MessageBody is null", messageExt);
                    return Result.getResult(Status.NO_RESULT);
                }
                // 特定类型使用自定义的classloader
                if (mqCloudConfigHelper.getClassList() != null &&
                        mqCloudConfigHelper.getClassList().contains(topic)) {
                    Thread.currentThread().setContextClassLoader(messageTypeClassLoader);
                }
                return Result.getResult(toDecodedMessage(messageExt, clusterInfo));
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
    public Result<MessageData> queryMessage(MessageQueryCondition messageQueryCondition, boolean offsetSearch) {
        List<DecodedMessage> messageList = null;
        MQPullConsumer consumer = null;
        try {
            // 获取消费者
            Cluster cluster = clusterService.getMQClusterById(messageQueryCondition.getCid());
            consumer = getConsumer(cluster);
            // 初始化参数
            if (messageQueryCondition.getMqOffsetList() == null) {
                List<MQOffset> mqOffsetList = getMQOffsetList(cluster, consumer, messageQueryCondition, true,
                        offsetSearch);
                if (mqOffsetList == null) {
                    return Result.getResult(Status.NO_RESULT);
                }
                messageQueryCondition.setMqOffsetList(mqOffsetList);
            }
            // 特定类型使用自定义的classloader
            if (mqCloudConfigHelper.getClassList() != null &&
                    mqCloudConfigHelper.getClassList().contains(messageQueryCondition.getTopic())) {
                Thread.currentThread().setContextClassLoader(messageTypeClassLoader);
            }
            messageList = new ArrayList<DecodedMessage>();
            for (MQOffset mqOffset : messageQueryCondition.getMqOffsetList()) {
                // 无消息跳过
                if (!mqOffset.hasMessage()) {
                    continue;
                }
                fetchMessage(consumer, messageQueryCondition, mqOffset, messageList, offsetSearch);
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
            List<DecodedMessage> messageList, boolean offsetSearch) throws Exception {
        while (mqOffset.hasMessage() && messageQueryCondition.needSearch()) {
            try {
                // 拉取消息
                PullResult pullResult = consumer.pull(mqOffset.getMq(), SUB_EXPRESSION, mqOffset.getOffset(), 32);
                // 防止offset不前进
                if (mqOffset.getOffset() < pullResult.getNextBeginOffset()) {
                    mqOffset.setOffset(pullResult.getNextBeginOffset());
                } else {
                    mqOffset.setOffset(mqOffset.getOffset() + 1);
                }
                // 无消息继续
                if (PullStatus.FOUND != pullResult.getPullStatus()) {
                    continue;
                }
                messageQueryCondition.setSearchedSize(messageQueryCondition.getSearchedSize()
                        + pullResult.getMsgFoundList().size());
                for (MessageExt msg : pullResult.getMsgFoundList()) {
                    // 过滤不在时间范围的消息
                    if (!offsetSearch && !messageQueryCondition.valid(msg.getBornTimestamp())) {
                        continue;
                    }
                    // 过滤不在当前offset查询条件内的消息
                    if (msg.getQueueOffset() > mqOffset.getMaxOffset()) {
                        continue;
                    }
                    byte[] bytes = msg.getBody();
                    if (bytes == null || bytes.length == 0) {
                        logger.warn("MessageExt={}, MessageBody is null", msg);
                        continue;
                    }
                    DecodedMessage m = toDecodedMessage(msg, mqOffset.getMq().getBrokerName());
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
     * 
     * @param msg
     * @return clusterInfo
     */
    private DecodedMessage toDecodedMessage(MessageExt msg, ClusterInfo clusterInfo) {
        String broker = null;
        if (clusterInfo != null) {
            HashMap<String, BrokerData> brokerAddressMap = clusterInfo.getBrokerAddrTable();
            InetSocketAddress inetSocketAddress = (InetSocketAddress) msg.getStoreHost();
            String addr = inetSocketAddress.getAddress().getHostAddress();
            for (BrokerData brokerData : brokerAddressMap.values()) {
                String master = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
                if (master != null) {
                    master = master.split(":")[0];
                    if (master.equals(addr)) {
                        broker = brokerData.getBrokerName();
                        break;
                    }
                }
            }
        }
        return toDecodedMessage(msg, broker);
    }

    /**
     * 转换为解码后的消息
     * 
     * @param msg
     * @return DecodedMessage
     */
    private DecodedMessage toDecodedMessage(MessageExt msg, String broker) {
        DecodedMessage m = new DecodedMessage();
        byte[] bytes = msg.getBody();
        msg.setBody(null);
        Object decodedBody = bytes;
        try {
            decodedBody = messageSerializer.deserialize(bytes);
            // 兼容rocketmq原生客户端未序列化消息
            if (decodedBody == null) {
                decodedBody = bytes;
            } else {
                m.setMessageBodySerializer(MessageSerializerEnum.PROTOSTUF);
            }
        } catch (Exception e) {
            logger.debug("deserialize topic:{} message err:{}", msg.getTopic(), e.getMessage());
            decodedBody = bytes;
        }
        // 将主体消息转换为String
        if (decodedBody instanceof byte[]) {
            m.setMessageBodyType(MessageBodyType.BYTE_ARRAY);
            if (CommonUtil.isTraceTopic(msg.getTopic())) {
                List<TraceContext> traceContextList = MsgTraceDecodeUtil
                        .decoderFromTraceDataString(new String((byte[]) decodedBody));
                m.setDecodedBody(JSON.toJSONString(traceContextList));
            } else {
                m.setDecodedBody(HtmlUtils.htmlEscape(new String((byte[]) decodedBody)));
            }
        } else if (decodedBody instanceof String) {
            m.setMessageBodyType(MessageBodyType.STRING);
            m.setDecodedBody(HtmlUtils.htmlEscape((String) decodedBody));
        } else if (decodedBody instanceof Map &&
                mqCloudConfigHelper.getMapWithByteList() != null &&
                !mqCloudConfigHelper.getMapWithByteList().contains(msg.getTopic())) {
            m.setMessageBodyType(MessageBodyType.Map);
            m.setDecodedBody(decodedBody.toString());
        } else {
            m.setMessageBodyType(MessageBodyType.OBJECT);
            m.setDecodedBody(JSON.toJSONString(decodedBody));
        }
        BeanUtils.copyProperties(msg, m);
        m.setBroker(broker);
        // 设置topic原始信息
        String realTopic = msg.getProperty(MessageConst.PROPERTY_REAL_TOPIC);
        String retryTopic = msg.getProperty(MessageConst.PROPERTY_RETRY_TOPIC);
        if(retryTopic == null) {
            m.setRealTopic(realTopic);
        } else {
            m.setRealTopic(retryTopic);
            m.setConsumer(realTopic);
        }
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
            MessageQueryCondition messageQueryCondition, boolean retryWithErr, boolean offsetSearch) {
        List<MQOffset> offsetList = null;
        try {
            Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(messageQueryCondition.getTopic());
            offsetList = new ArrayList<MQOffset>();
            for (MessageQueue mq : mqs) {
                // 判断当前消息队列是否符合条件
                if (!isContainsBrokerOrQueue(mq, messageQueryCondition.getBrokerName(),
                        messageQueryCondition.getQueueId())) {
                    continue;
                }
                long minOffset = 0;
                long maxOffset = 0;
                try {
                    if (!offsetSearch) {
                        minOffset = consumer.searchOffset(mq, messageQueryCondition.getStart());
                        maxOffset = consumer.searchOffset(mq, messageQueryCondition.getEnd());
                        // 处理非法情况
                        if (minOffset >= maxOffset) {
                            if (minOffset == 0) {
                                maxOffset = 1;
                            } else {
                                minOffset = maxOffset - 1;
                            }
                        }
                    } else {
                        maxOffset = messageQueryCondition.getEnd();
                        minOffset = messageQueryCondition.getStart();
                        long tmpMaxOffset = consumer.maxOffset(mq);
                        if (maxOffset > tmpMaxOffset) {
                            maxOffset = tmpMaxOffset;
                        }
                        long tmpMinOffset = consumer.minOffset(mq);
                        if (minOffset < tmpMinOffset) {
                            minOffset = tmpMinOffset;
                        }
                    }
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
            if (retryWithErr
                    && e instanceof MQClientException
                    && e.getMessage().contains("Can not find Message Queue for this topic")) {
                TopicConfig topicConfig = null;
                if (messageQueryCondition.getTopic().startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX)) {
                    TopicRouteData topicRouteData = topicService.route(cluster, messageQueryCondition.getTopic());
                    List<QueueData> queueDatas = topicRouteData.getQueueDatas();
                    QueueData queueData = queueDatas.get(0);
                    topicConfig = new TopicConfig();
                    topicConfig.setTopicName(messageQueryCondition.getTopic());
                    topicConfig.setWriteQueueNums(queueData.getWriteQueueNums());
                    topicConfig.setReadQueueNums(queueData.getReadQueueNums());
                    topicConfig.setTopicSysFlag(queueData.getTopicSynFlag());
                } else if (messageQueryCondition.getTopic().equals("SCHEDULE_TOPIC_XXXX")) {
                    topicConfig = new TopicConfig();
                    topicConfig.setTopicName("SCHEDULE_TOPIC_XXXX");
                    int nums = MessageDelayLevel.values().length;
                    topicConfig.setWriteQueueNums(nums);
                    topicConfig.setReadQueueNums(nums);
                }
                if (topicConfig != null) {
                    Result<?> result = topicService.createAndUpdateTopicOnCluster(cluster, topicConfig);
                    if (result.isNotOK()) {
                        logger.warn("change topic:{} perm failed, {}", topicConfig.getTopicName(), result);
                    } else {
                        // 尝试一次
                        return getMQOffsetList(cluster, consumer, messageQueryCondition, false, offsetSearch);
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
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP);
        consumer.setNamesrvAddr(Joiner.on(";").join(nsList));
        consumer.setVipChannelEnabled(mqCluster.isEnableVipChannel());
        consumer.start();
        // 是否从slave查询消息
        if (mqCloudConfigHelper.isQueryMessageFromSlave()) {
            consumer.getDefaultMQPullConsumerImpl().getPullAPIWrapper().setConnectBrokerByUser(true);
            consumer.getDefaultMQPullConsumerImpl().getPullAPIWrapper().setDefaultBrokerId(MixAll.MASTER_ID + 1);
        }
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
                    if (isBroadcast) {
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
        for (MessageQueue messageQueue : offsetTable.keySet()) {
            consumerOffsetMap.put(messageQueue, offsetTable.get(messageQueue).getConsumerOffset());
        }

        return consumed(consumerOffsetMap, mp, brokerAddrTable);
    }

    /**
     * 广播模式，需要判断每个链接
     * 
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
        for (Connection conn : connSet) {
            // 抓取状态
            Map<MessageQueue, Long> mqOffsetMap = consumerService.fetchConsumerStatus(mqAdmin, messageParam.getTopic(),
                    consumer, conn);
            if (mqOffsetMap == null) {
                return false;
            }
            if (!consumed(mqOffsetMap, messageParam, brokerAddrTable)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否消费了
     * 
     * @param consumerOffsetMap
     * @param messageParam
     * @param brokerAddrTable
     * @return
     */
    private boolean consumed(Map<MessageQueue, Long> consumerOffsetMap, MessageParam messageParam,
            Map<String, BrokerData> brokerAddrTable) {
        for (MessageQueue messageQueue : consumerOffsetMap.keySet()) {
            // topic不一致
            if (!messageQueue.getTopic().equals(messageParam.getTopic())) {
                continue;
            }
            // queueid不一致
            if (messageQueue.getQueueId() != messageParam.getQueueId()) {
                continue;
            }
            // offset太小
            if (consumerOffsetMap.get(messageQueue) <= messageParam.getQueueOffset()) {
                continue;
            }
            BrokerData brokerData = brokerAddrTable.get(messageQueue.getBrokerName());
            if (brokerData == null) {
                continue;
            }
            String addr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
            if (addr.equals(messageParam.getStoreHost())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重发消息
     * 
     * @param cluster
     * @param topic
     * @param msgId
     * @param consumer 消息会发往%RETRY%consumer中
     * @return Result<SendResult>
     */
    public Result<SendResult> resend(Cluster cluster, String topic, String msgId, String consumer) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<SendResult>>() {
            public Result<SendResult> callback(MQAdminExt mqAdmin) throws Exception {
                MessageExt messageExt = mqAdmin.viewMessage(topic, msgId);
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                Message message = new Message(MixAll.getRetryTopic(consumer), messageExt.getTags(),
                        messageExt.getKeys(), messageExt.getBody());
                String originMsgId = MessageAccessor.getOriginMessageId(messageExt);
                MessageAccessor.setOriginMessageId(message,
                        UtilAll.isBlank(originMsgId) ? messageExt.getMsgId() : originMsgId);
                message.setFlag(messageExt.getFlag());
                MessageAccessor.setProperties(message, messageExt.getProperties());
                MessageAccessor.putProperty(message, MessageConst.PROPERTY_RETRY_TOPIC, topic);
                MessageAccessor.setReconsumeTime(message, "1");
                MessageAccessor.setMaxReconsumeTimes(message, "3");
                message.setDelayTimeLevel(1);
                SendResult sr = sohuMQAdmin.sendMessage(message);
                return Result.getResult(sr);
            }

            public Result<SendResult> exception(Exception e) throws Exception {
                logger.error("cluster:{} topic:{} msgId:{}, send msg err", cluster, topic, msgId, e);
                return Result.getDBErrorResult(e);
            }

            public Cluster mqCluster() {
                return cluster;
            }
        });
    }
    
    /**
     * 直接发送消息
     * @param cluster
     * @param msgId
     * @param consumer
     * @return
     */
    public Result<List<ResentMessageResult>> resendDirectly(Cluster cluster, String msgId, String consumer) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<List<ResentMessageResult>>>() {
            public Result<List<ResentMessageResult>> callback(MQAdminExt mqAdmin) throws Exception {
                ConsumerConnection ccs = mqAdmin.examineConsumerConnectionInfo(consumer);
                List<ResentMessageResult> list = new ArrayList<>();
                Result<List<ResentMessageResult>> allResult = Result.getOKResult();
                for (Connection conn : ccs.getConnectionSet()) {
                    ConsumeMessageDirectlyResult result = mqAdmin.consumeMessageDirectly(consumer, conn.getClientId(),
                            msgId);
                    ResentMessageResult resentMessageResult = new ResentMessageResult(conn.getClientId(), result);
                    list.add(resentMessageResult);
                    if (!resentMessageResult.isOK()) {
                        allResult.setStatus(Status.WEB_ERROR.getKey());
                    }
                }
                allResult.setResult(list);
                return allResult;
            }

            public Result<List<ResentMessageResult>> exception(Exception e) throws Exception {
                logger.error("cluster:{} consumer:{} msgId:{}, send msg err", cluster, consumer, msgId, e);
                return Result.getWebErrorResult(e);
            }

            public Cluster mqCluster() {
                return cluster;
            }
        });
    }

    /**
     * 根据参数brokerName和queueId过滤要拉取消息的MessageQueue
     * 
     * @param mqOffsetList
     * @param brokerName
     * @param queueId
     */
    private boolean isContainsBrokerOrQueue(MessageQueue mq, String brokerName, Integer queueId) {
        // brokerName为空时说明不指定broker进行查询
        if (StringUtils.isBlank(brokerName)) {
            return true;
        }
        if (mq.getBrokerName().equals(brokerName)
                && (queueId == null || (queueId != null && mq.getQueueId() == queueId))) {
            return true;
        }
        return false;
    }

    /**
     * 对trace消息进行分组,并过滤不符合要求的消息
     * 
     * @param decodedMessageList
     * @return
     */
    public Map<String, TraceViewVO> groupTraceMessage(List<DecodedMessage> decodedMessageList, String msgKey) {
        if (decodedMessageList == null) {
            return null;
        }
        // 保存分组结果
        Map<String, TraceViewVO> msgTraceViewMap = new TreeMap<String, TraceViewVO>();
        // 解析消息
        for (DecodedMessage decodedMessage : decodedMessageList) {
            String message = decodedMessage.getDecodedBody();
            // 转换为trace对象
            List<TraceContext> traceContextList = JSON.parseArray(message, TraceContext.class);
            for (TraceContext traceContext : traceContextList) {
                TraceBean traceBean = traceContext.getTraceBeans().get(0);
                // 过滤非相关消息
                if (!msgKey.equals(traceBean.getMsgId()) && !msgKey.equals(traceBean.getKeys())) {
                    continue;
                }
                TraceViewVO traceViewVO = msgTraceViewMap.get(traceBean.getMsgId());
                if (traceViewVO == null) {
                    traceViewVO = new TraceViewVO();
                    msgTraceViewMap.put(traceBean.getMsgId(), traceViewVO);
                }
                // 构建消息详情
                TraceMessageDetail traceMessageDetail = new TraceMessageDetail();
                BeanUtils.copyProperties(traceContext, traceMessageDetail);
                BeanUtils.copyProperties(traceBean, traceMessageDetail);
                // ClientHost统一使用消息中的bornHost
                traceMessageDetail.setClientHost(decodedMessage.getBornHostString());
                // 发送消息部分
                if (TraceType.Pub == traceContext.getTraceType()) {
                    traceViewVO.buildProducer(traceMessageDetail, traceContext);
                } else { // 消费消息部分
                    // 不显示空字符串
                    if (traceMessageDetail.getTopic().isEmpty()) {
                        traceMessageDetail.setTopic(null);
                    }
                    // 消费者不显示broker信息
                    traceMessageDetail.setBroker(null);
                    if (TraceType.SubBefore == traceContext.getTraceType()) { // 实际消费动作前
                        traceMessageDetail.setSuccess(null);
                        traceMessageDetail.setCostTime(null);
                    } else if (TraceType.SubAfter == traceContext.getTraceType()) { // 消费结束
                        traceMessageDetail.setTimeStamp(0);
                    }
                    traceViewVO.buildConsumer(traceMessageDetail, traceContext);
                }
            }
        }
        // 排序
        for (TraceViewVO traceViewVO : msgTraceViewMap.values()) {
            List<RequestViewVO> consumerRequestViewList = traceViewVO.getConsumerRequestViewList();
            if (consumerRequestViewList != null) {
                Collections.sort(consumerRequestViewList);
            }
        }
        return msgTraceViewMap;
    }
    
    /**
     * 配置改变
     */
    @EventListener
    public void configChange(MQCloudConfigEvent mqCloudConfigEvent) {
        if (mqCloudConfigHelper.getMessageTypeLocation() == null) {
            return;
        }
        if (messageTypeClassLoader == null) {
            initMessageTypeClassLoader(false);
            return;
        }
        if (mqCloudConfigHelper.getMessageTypeLocation().equals(messageTypeClassLoader.getMessageTypeLocation())) {
            return;
        }
        logger.info("messageTypeLocation changed from {} to {}", messageTypeClassLoader.getMessageTypeLocation(),
                mqCloudConfigHelper.getMessageTypeLocation());
        initMessageTypeClassLoader(true);
    }
    
    private void initMessageTypeClassLoader(boolean clearCache) {
        MessageTypeClassLoader tmp = messageTypeClassLoader;
        boolean initOK = true;
        try {
            messageTypeClassLoader = new MessageTypeClassLoader(mqCloudConfigHelper.getMessageTypeLocation());
        } catch (Exception e) {
            logger.error("init {}", mqCloudConfigHelper.getMessageTypeLocation(), e);
            initOK = false;
        }
        if (initOK && clearCache) {
            for (String className : tmp.getClassNameUrlMap().keySet()) {
                Object result = MQCloudIdStrategy.removeSchema(className);
                if (result != null) {
                    logger.info("clear class cache {}", className);
                }
            }
        }
    }
}
