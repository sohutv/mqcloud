package com.sohu.tv.mq.cloud.service;

import com.google.common.base.Joiner;
import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.bo.DecodedMessage.MessageBodyType;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.*;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper.MQCloudConfigEvent;
import com.sohu.tv.mq.cloud.web.controller.param.MessageParam;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.vo.TraceViewVO;
import com.sohu.tv.mq.cloud.web.vo.TraceViewVO.RequestViewVO;
import com.sohu.tv.mq.serializable.DefaultMessageSerializer;
import com.sohu.tv.mq.serializable.MessageSerializer;
import com.sohu.tv.mq.serializable.MessageSerializerEnum;
import com.sohu.tv.mq.util.CommonUtil;
import com.sohu.tv.mq.util.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import org.apache.rocketmq.common.message.*;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.Connection;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.QueueData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.apache.rocketmq.tools.admin.api.TrackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static final String RMQ_SYS_WHEEL_TIMER = "rmq_sys_wheel_timer";

    public static final String DEFAULT_CANCEL_MESSAGE_TAGS = "wheelTime_message_cancel";

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

    @Autowired
    private CancelUniqIdService cancelUniqIdService;

    @Autowired
    private BrokerService brokerService;

    public Result<List<DecodedMessage>> queryMessageByKey(Cluster cluster, String topic, String key, long begin,
                                                          long end) {
        return queryMessageByKey(cluster, topic, key, begin, end, false);
    }

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
            long end, boolean isUniqueKey) {
        Result<?> clusterInfoResult = examineBrokerClusterInfo(cluster);
        if (clusterInfoResult.isNotOK()) {
            return (Result<List<DecodedMessage>>) clusterInfoResult;
        }
        ClusterInfo clusterInfo = (ClusterInfo) clusterInfoResult.getResult();
        return mqAdminTemplate.execute(new MQAdminCallback<Result<List<DecodedMessage>>>() {
            public Result<List<DecodedMessage>> callback(MQAdminExt mqAdmin) throws Exception {
                // 其实这里只能查询64条消息，broker端有限制
                QueryResult queryResult = null;
                if (isUniqueKey) {
                    SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                    queryResult = sohuMQAdmin.queryMessageByUniqKey(topic, key, 32, begin, end);
                } else {
                    queryResult = mqAdmin.queryMessage(topic, key, 100, begin, end);
                }
                List<DecodedMessage> decodedMessages = decodeMessages(queryResult, topic, clusterInfo);
                if (decodedMessages == null ) {
                    logger.warn("msg list is null, cluster:{} topic:{} msgId:{}", cluster, topic, key);
                    return Result.getResult(Status.NO_RESULT);
                }
                return Result.getResult(decodedMessages);
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
        Result<?> messageExtResult = viewMessageByUniqKey(cluster, topic, msgId);
        if(messageExtResult.isNotOK()){
            messageExtResult = viewMessage(cluster, topic, msgId);
        }
        if (messageExtResult.isNotOK()) {
            return (Result<DecodedMessage>) messageExtResult;
        }
        MessageExt messageExt = (MessageExt) messageExtResult.getResult();
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
        // 获取broker集群信息
        ClusterInfo clusterInfo = examineBrokerClusterInfo(cluster).getResult();
        return Result.getResult(toDecodedMessage(messageExt, clusterInfo));
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
            // 队列为空或者剩余消息不多且还有未搜索的队列，获取队列偏移量
            if (messageQueryCondition.getMqOffsetList() == null
                    || (messageQueryCondition.getLeftSize() <= 32
                    && messageQueryCondition.getTotalQueueNum() > messageQueryCondition.getMqOffsetList().size())) {
                List<MQOffset> mqOffsetList = getMQOffsetList(cluster, consumer, messageQueryCondition, true,
                        offsetSearch);
                if (mqOffsetList == null) {
                    return Result.getResult(Status.NO_RESULT);
                }
                if (messageQueryCondition.getMqOffsetList() == null) {
                    messageQueryCondition.setMqOffsetList(mqOffsetList);
                } else {
                    messageQueryCondition.getMqOffsetList().addAll(mqOffsetList);
                }
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
            // 如果是查询定时消息，则可能出现滚动重复消息，需要去重
            if (messageQueryCondition.isTimerWheelSearch() && !messageQueryCondition.isShowSysMessage()) {
                messageList = deduplicateRollWheelMessage(messageList);
                // 重置记录数
                messageQueryCondition.setCurSize(messageList.size());
            } else {
                // 排序
                sort(messageList, MessageExt::getStoreTimestamp);
            }
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
     * 抓取时间轮定时消息
     *
     * @param cluster
     * @param key
     * @param broker
     * @param begin
     * @param end
     * @param isUniqueKey
     * @return
     * @throws MQClientException
     */
    public Result<List<DecodedMessage>> queryTimerMessage(Cluster cluster,String key,
                                                          String broker, long begin,
                                                          long end, boolean isUniqueKey) {
        Result<?> clusterInfoResult = examineBrokerClusterInfo(cluster);
        if (clusterInfoResult.isNotOK()) {
            return (Result<List<DecodedMessage>>) clusterInfoResult;
        }
        ClusterInfo clusterInfo = (ClusterInfo) clusterInfoResult.getResult();
        return mqAdminTemplate.execute(new MQAdminCallback<Result<List<DecodedMessage>>>() {
            @Override
            public Result<List<DecodedMessage>> callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                QueryResult queryResult = sohuMQAdmin.queryTimerMessageByUniqKey(broker, key, begin, end, isUniqueKey);
                List<DecodedMessage> decodedMessages = decodeMessages(queryResult, RMQ_SYS_WHEEL_TIMER, clusterInfo);
                if (decodedMessages == null) {
                    return Result.getResult(Status.NO_RESULT);
                }
                return Result.getResult(decodedMessages);
            }

            public Result<List<DecodedMessage>> exception(Exception e) throws Exception {
                logger.error("queryMessage cluster:{}  key:{}, err:{}", cluster, key, e.getMessage());
                return Result.getWebErrorResult(e);
            }

            public Cluster mqCluster() {
                return cluster;
            }
        });
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
                boolean useStoreTime = messageQueryCondition.useStoreTime();
                for (MessageExt msg : pullResult.getMsgFoundList()) {
                    // 过滤不在时间范围的消息
                    long time = msg.getBornTimestamp();
                    if (useStoreTime) {
                        time = msg.getStoreTimestamp();
                    }
                    if (!offsetSearch && !messageQueryCondition.valid(time)) {
                        continue;
                    }
                    // 过滤不在当前offset查询条件内的消息
                    if (msg.getQueueOffset() > mqOffset.getMaxOffset()) {
                        continue;
                    }
                    // 定时消息搜索需要过滤真实topic
                    if (messageQueryCondition.isTimerWheelSearch() &&
                            !messageQueryCondition.getTopic().equals(msg.getProperty(MessageConst.PROPERTY_REAL_TOPIC))) {
                        continue;
                    }
                    byte[] bytes = msg.getBody();
                    if (bytes == null || bytes.length == 0) {
                        logger.warn("MessageExt={}, MessageBody is null", msg);
                        continue;
                    }
                    // 是否过滤系统消息
                    String tags = msg.getTags();
                    if (!messageQueryCondition.isShowSysMessage() && DEFAULT_CANCEL_MESSAGE_TAGS.equals(tags)) {
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
     * 解析查询消息
     *
     * @param queryResult
     * @param topic
     * @param clusterInfo
     * @return
     */
    private List<DecodedMessage> decodeMessages(QueryResult queryResult, String topic, ClusterInfo clusterInfo) {
        List<MessageExt> messageExtList = queryResult.getMessageList();
        if (messageExtList == null || messageExtList.size() == 0) {
            return null;
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
        return messageList;
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
            Map<String, BrokerData> brokerAddressMap = clusterInfo.getBrokerAddrTable();
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
    public DecodedMessage toDecodedMessage(MessageExt msg, String broker) {
        DecodedMessage m = new DecodedMessage();
        byte[] bytes = msg.getBody();
        m.setMsgLength(bytes.length);
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
                m.setDecodedBody(JSONUtil.toJSONString(traceContextList));
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
            m.setDecodedBody(JSONUtil.toJSONString(decodedBody));
        }
        // fix json 反序列化空指针异常
        try {
            msg.getDeliverTimeMs();
        } catch (Exception e) {
            msg.setDeliverTimeMs(-1);
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
        // 设置延迟投递时间
        String timerDeliverTime = msg.getProperty("TIMER_DELIVER_MS");
        if (timerDeliverTime != null) {
            m.setTimerDeliverTime(NumberUtils.toLong(timerDeliverTime));
        }
        // 设置滚动次数(时间轮定时消息特有属性)
        String times = Optional.ofNullable(msg.getProperty("TIMER_ROLL_TIMES")).orElse("0");
        m.setTimerRollTimes(NumberUtils.toInt(times));
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
            String topic = messageQueryCondition.getTopic();
            if (messageQueryCondition.isTimerWheelSearch()) {
                topic = RMQ_SYS_WHEEL_TIMER;
            }
            Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(topic);
            messageQueryCondition.setTotalQueueNum(mqs.size());
            if (offsetSearch) {
                // 偏移量搜索，过滤掉不符合条件的消息队列
                mqs = mqs.stream().filter(mq -> isContainsBrokerOrQueue(mq, messageQueryCondition.getBrokerName(),
                        messageQueryCondition.getQueueId())).collect(Collectors.toSet());
            } else {
                // 普通搜索，第一次最多搜索50个队列
                if (messageQueryCondition.getMqOffsetList() == null) {
                    if (mqs.size() > mqCloudConfigHelper.getMaxQueueNumOfFirstSearch()) {
                        mqs = mqs.stream().limit(mqCloudConfigHelper.getMaxQueueNumOfFirstSearch()).collect(Collectors.toSet());
                    }
                } else {
                    // 普通搜索，第二次开始，过滤掉已经搜索过的队列
                    mqs = mqs.stream().filter(mq -> messageQueryCondition.getMqOffsetList().stream()
                            .noneMatch(mqOffset -> mqOffset.getMq().equals(mq)))
                            .collect(Collectors.toSet());
                }
            }
            offsetList = new ArrayList<MQOffset>();
            for (MessageQueue mq : mqs) {
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
                                maxOffset = minOffset + 1;
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
                    topicConfig.setTopicSysFlag(queueData.getTopicSysFlag());
                } else if (messageQueryCondition.getTopic().equals("SCHEDULE_TOPIC_XXXX")) {
                    topicConfig = new TopicConfig();
                    topicConfig.setTopicName("SCHEDULE_TOPIC_XXXX");
                    int nums = MessageDelayLevel.values().length;
                    topicConfig.setWriteQueueNums(nums);
                    topicConfig.setReadQueueNums(nums);
                } else if (messageQueryCondition.isTimerWheelSearch()) {
                    topicConfig = new TopicConfig();
                    topicConfig.setTopicName(RMQ_SYS_WHEEL_TIMER);
                    topicConfig.setWriteQueueNums(1);
                    topicConfig.setReadQueueNums(1);
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
    private void sort(List<DecodedMessage> messageList, Function<MessageExt, Long> function) {
        Collections.sort(messageList, new Comparator<MessageExt>() {
            public int compare(MessageExt o1, MessageExt o2) {
                if (function.apply(o1) - function.apply(o2) == 0) {
                    return 0;
                }
                return (function.apply(o1) > function.apply(o2)) ? -1 : 1;
            }
        });
    }

    /**
     * 滚动消息过滤
     *
     * @param messageList
     * @return List<DecodedMessage>
     */
    private List<DecodedMessage> deduplicateRollWheelMessage(List<DecodedMessage> messageList) {
        List<DecodedMessage> result = new ArrayList<>();
        Map<String, List<DecodedMessage>> decodeGroup = messageList.stream().collect(Collectors.groupingBy(DecodedMessage::getMsgId));
        for (Entry<String, List<DecodedMessage>> entry : decodeGroup.entrySet()) {
            List<DecodedMessage> value = entry.getValue();
            if (value.size() > 1) {
                DecodedMessage decodedMessage = value.stream()
                        .sorted(Comparator.comparing(DecodedMessage::getTimerRollTimes))
                        .findFirst().get();
                result.add(decodedMessage);
            }else {
                result.addAll(value);
            }
        }
        sort(result, MessageExt::getBornTimestamp);
        return result;
    }

    /**
     * 消息轨迹
     * 
     * @param msg
     * @return
     */
    public Result<?> track(MessageParam mp, PaginationParam paginationParam) {
        // 获取集群
        Cluster mqCluster = clusterService.getMQClusterById(mp.getCid());
        if (mqCluster == null) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        // 获取消费者
        Result<List<Consumer>> consumerListResult = consumerService.queryByTid(mp.getTid());
        if (consumerListResult.isEmpty()) {
            return Result.getResult(Status.NO_RESULT);
        }
        List<Consumer> consumerList = consumerListResult.getResult();
        paginationParam.caculatePagination(consumerList.size());
        consumerList.sort(new Comparator<Consumer>() {
            public int compare(Consumer o1, Consumer o2) {
                return (int) (o1.getId() - o2.getId());
            }
        });
        consumerList = consumerList.subList(paginationParam.getBegin(), paginationParam.getEnd());
        // 查询消息轨迹
        List<MessageTrack> result = new ArrayList<MessageTrack>();
        for (Consumer consumer : consumerList) {
            MessageTrack mt = messageTrack(mqCluster, consumer, mp);
            result.add(mt);
        }
        return Result.getResult(result);
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
    private MessageTrackExt messageTrack(Cluster cluster, Consumer consumer, MessageParam mp) {
        MessageTrackExt mt = new MessageTrackExt();
        mt.setConsumerGroup(consumer.getName());
        mt.setTrackType(TrackType.UNKNOWN);
        // 检查消费者是否在线
        Result<ConsumerConnection> result = consumerService.examineConsumerConnectionInfo(consumer.getName(), cluster,
                consumer.isProxyRemoting());
        ConsumerConnection cc = result.getResult();
        if (cc == null) {
            if (Status.NO_ONLINE.getKey() == result.getStatus()) {
                mt.setTrackType(TrackType.NOT_ONLINE);
            }
            mt.setExceptionDesc("ERROR:" + result.getException().toString());
            return mt;
        }

        // 检查定时消息
        if (mp.getMsgId() != null) {
            Result<MessageExt> messageExtResult = viewMessage(cluster, mp.getTopic(), mp.getMsgId());
            MessageExt messageExt = messageExtResult.getResult();
            if (messageExt != null) {
                mp.setQueueId(messageExt.getQueueId());
                mp.setOffset(messageExt.getQueueOffset());
                InetSocketAddress inetSocketAddress = (InetSocketAddress) messageExt.getStoreHost();
                mp.setStoreHost(inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort());
            } else {
                setException(mt, messageExtResult.getException());
                return mt;
            }
        }

        ClusterInfo ci = examineBrokerClusterInfo(cluster).getResult();
        switch (cc.getConsumeType()) {
            case CONSUME_ACTIVELY:
                mt.setTrackType(TrackType.PULL);
                break;
            case CONSUME_PASSIVELY:
                Result<?> consumeResult = null;
                if (consumer.isBroadcast()) {
                    consumeResult = consumed(cluster, cc.getConnectionSet(), mp, consumer, ci.getBrokerAddrTable());
                } else {
                    consumeResult = consumed(cluster, mp, consumer.getName(), ci.getBrokerAddrTable());
                }
                if (consumeResult.isNotOK()) {
                    setException(mt, consumeResult.getException());
                    return mt;
                }

                if ((Boolean) consumeResult.getResult()) {
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

    private void setException(MessageTrackExt mt, Exception e) {
        if (e == null) {
            return;
        }
        if (e instanceof MQClientException) {
            MQClientException mqClientException = (MQClientException) e;
            if (208 == mqClientException.getResponseCode()) {
                mt.setTrackType(TrackType.NOT_CONSUME_YET);
            } else {
                mt.setExceptionDesc("CODE:" + mqClientException.getResponseCode() + " DESC:" + mqClientException.getErrorMessage());
            }
        } else if (e instanceof MQBrokerException) {
            MQBrokerException mqBrokerException = (MQBrokerException) e;
            mt.setExceptionDesc("CODE:" + mqBrokerException.getResponseCode() + " DESC:" + mqBrokerException.getErrorMessage());
        } else {
            mt.setExceptionDesc(UtilAll.exceptionSimpleDesc(e));
        }
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
    private Result<?> consumed(Cluster cluster, MessageParam mp, String group, Map<String, BrokerData> brokerAddrTable) {
        Result<?> consumeStatsResult = consumerService.examineConsumeStats(cluster, group);
        if (consumeStatsResult.isNotOK()) {
            return consumeStatsResult;
        }
        ConsumeStats consumeStats = (ConsumeStats) consumeStatsResult.getResult();
        Map<MessageQueue, OffsetWrapper> offsetTable = consumeStats.getOffsetTable();
        Map<MessageQueue, Long> consumerOffsetMap = new HashMap<>();
        for (MessageQueue messageQueue : offsetTable.keySet()) {
            consumerOffsetMap.put(messageQueue, offsetTable.get(messageQueue).getConsumerOffset());
        }
        return Result.getResult(consumed(consumerOffsetMap, mp, brokerAddrTable));
    }

    /**
     * @description 检查是否存在取消消息
     * @param topic
     * @param uniqId
     * @param beginTime
     * @param cluster
     * @return com.sohu.tv.mq.cloud.util.Result<java.lang.Boolean>
     * @author fengwang219475
     * @date 2023/7/13 09:27:00
     */
    public Result<Boolean> checkCancelMessageByKey(String topic, String uniqId, long beginTime, Cluster cluster) {
        String keys = topic + "_" + uniqId;
        Result<List<DecodedMessage>> cancelMsgResult = this.queryMessageByKey(cluster, RMQ_SYS_WHEEL_TIMER, keys,
                beginTime, Long.MAX_VALUE, false);
        if (cancelMsgResult.isNotOK()) {
            return Result.getWebErrorResult(cancelMsgResult.getException());
        }
        if (cancelMsgResult.isNotEmpty()) {
            return Result.getResult(true);
        }
        return Result.getResult(false);
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
    private Result<?> consumed(Cluster cluster, Set<Connection> connSet, MessageParam messageParam, Consumer consumer,
                               Map<String, BrokerData> brokerAddrTable) {
        for (Connection conn : connSet) {
            // 抓取状态
            Result<Map<MessageQueue, Long>> mqOffsetMapResult = (Result<Map<MessageQueue, Long>>)
                    consumerService.fetchConsumerStatus(cluster, messageParam.getTopic(), consumer.getName(), conn,
                            consumer.isProxyRemoting());
            if (mqOffsetMapResult.isNotOK()) {
                return mqOffsetMapResult;
            }
            if (!consumed(mqOffsetMapResult.getResult(), messageParam, brokerAddrTable)) {
                return Result.getResult(false);
            }
        }
        return Result.getResult(true);
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
     * 发送定时消息取消消息
     * 需要增加事务，如果发送失败，回滚cancelUniqId表插入，保证该消息能被下次申请取消
     * 如果消息在broker端落地成功，但响应失败，也回滚，controller端已对该类消息进行兼容处理
     *
     * @param cluster 集群
     * @param topic 主题
     * @param uniqId msgId
     * @param brokerName brokerName
     * @param deliveryTime 延迟时间
     * @return Result<SendResult>
     */
    @Transactional
    public Result<SendResult> sendWheelCancelMsgAndSaveCancelUniqId(Cluster cluster, Topic topic,
                                                 String uniqId, String brokerName,
                                                 Long deliveryTime) {
        cancelUniqIdService.save(topic.getId(), uniqId);
        Result<SendResult> sendResultResult = sendWheelCancelMsg(cluster, topic.getName(), uniqId, brokerName, deliveryTime);
        if (sendResultResult.isNotOK()) {
            throw new RuntimeException(sendResultResult.getException());
        }
        return sendResultResult;
    }


    /**
     * 指定MessageQueue发送定时消息取消消息
     *
     * @param cluster
     * @param topic
     * @param uniqId
     * @param brokerName
     * @param deliveryTime
     * @return Result<SendResult>
     */
    public Result<SendResult> sendWheelCancelMsg(Cluster cluster, String topic,
                                                 String uniqId, String brokerName,
                                                 Long deliveryTime) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<SendResult>>() {
            public Result<SendResult> callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                // 构建消息体
                Message message = new Message(topic, DEFAULT_CANCEL_MESSAGE_TAGS, topic + "_" + uniqId, "时间轮定时消息取消类消息".getBytes());
                MessageAccessor.putProperty(message, "TIMER_DEL_UNIQKEY", uniqId);
                MessageAccessor.putProperty(message, "TIMER_DELIVER_MS", deliveryTime + "");
                MessageQueue messageQueue = new MessageQueue(topic, brokerName, 0);
                // 发送消息
                SendResult sr = sohuMQAdmin.sendMessage(message, messageQueue);
                return Result.getResult(sr);
            }

            public Result<SendResult> exception(Exception e) throws Exception {
                logger.error("cluster:{} topic:{} uniqId:{}, send msg err", cluster, topic, uniqId, e);
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
    public Result<?> resendDirectly(Cluster cluster, String topic, String msgId, String consumer,
                                    boolean isProxyRemoting) {
        // 获取consumer连接
        Result<ConsumerConnection> consumerConnectionResult = consumerService.examineConsumerConnectionInfo(consumer,
                cluster, isProxyRemoting);
        ConsumerConnection consumerConnection = consumerConnectionResult.getResult();
        if (consumerConnection == null) {
            return consumerConnectionResult;
        }
        Set<Connection> connectionSet = consumerConnection.getConnectionSet();
        if (isProxyRemoting) {
            Result<MessageExt> messageExtResult = viewMessage(cluster, topic, msgId);
            MessageExt messageExt = messageExtResult.getResult();
            if (messageExt == null) {
                return Result.getResult(Status.NO_RESULT);
            }
            InetSocketAddress inetSocketAddress = (InetSocketAddress) messageExt.getStoreHost();
            String brokerAddr = inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
            Properties properties = brokerService.fetchBrokerConfig(cluster, brokerAddr).getResult();
            if (properties == null) {
                return Result.getResult(Status.NO_RESULT);
            }
            messageExt.setBrokerName(properties.getProperty("brokerName"));
            return resendDirectly(connectionSet, cluster, msgId, consumer, true, (admin, clientId) -> {
                try {
                    SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) admin;
                    return sohuMQAdmin.consumeMessageDirectlyOfProxy(topic, consumer, clientId, msgId, messageExt);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return resendDirectly(connectionSet, cluster, msgId, consumer, false, (admin, clientId) -> {
            try {
                return admin.consumeMessageDirectly(consumer, clientId, topic, msgId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 直接发送消息
     * @param connectionSet
     * @param cluster
     * @param msgId
     * @param consumer
     * @param isProxyRemoting
     * @param sendFun
     * @return
     */
    public Result<List<ResentMessageResult>> resendDirectly(Set<Connection> connectionSet, Cluster cluster, String msgId,
                                                            String consumer, boolean isProxyRemoting,
                                                            BiFunction<MQAdminExt, String, ConsumeMessageDirectlyResult> sendFun) {
        Result<List<ResentMessageResult>> allResult = Result.getOKResult();
        List<ResentMessageResult> list = new ArrayList<>();
        for (Connection conn : connectionSet) {
            ConsumeMessageDirectlyResult result = mqAdminTemplate.execute(new MQAdminCallback<ConsumeMessageDirectlyResult>() {
                public ConsumeMessageDirectlyResult callback(MQAdminExt mqAdmin) throws Exception {
                    return sendFun.apply(mqAdmin, conn.getClientId());
                }

                public ConsumeMessageDirectlyResult exception(Exception e) throws Exception {
                    logger.error("cluster:{} consumer:{} msgId:{}, send msg err", cluster, consumer, msgId, e);
                    return null;
                }

                public Cluster mqCluster() {
                    return cluster;
                }

                @Override
                public boolean isProxyRemoting() {
                    return isProxyRemoting;
                }
            });
            ResentMessageResult resentMessageResult = new ResentMessageResult(conn.getClientId(), result);
            list.add(resentMessageResult);
            if (!resentMessageResult.isOK()) {
                allResult.setStatus(Status.WEB_ERROR.getKey());
            }
        }
        allResult.setResult(list);
        return allResult;
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
            List<TraceContext> traceContextList = JSONUtil.parseList(message, TraceContext.class);
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

    public Result<MessageExt> viewMessage(Cluster cluster, String topic, String msgId) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<MessageExt>>() {
            public Result<MessageExt> callback(MQAdminExt mqAdmin) throws Exception {
                return Result.getResult(mqAdmin.viewMessage(topic, msgId));
            }
            public Result<MessageExt> exception(Exception e) throws Exception {
                logger.error("queryMessage cluster:{} topic:{} msgId:{}", cluster, topic, msgId, e);
                return Result.getWebErrorResult(e);
            }
            public Cluster mqCluster() {
                return cluster;
            }
        });
    }

    public Result<MessageExt> viewMessageByUniqKey(Cluster cluster, String topic, String msgId) {
            try {
                // 检查是否是uniqKey
                MessageDecoder.decodeMessageId(msgId);
                return Result.getResult(Status.NO_RESULT);
            } catch (Exception e) {
            }
            long beginQueryTime = MessageClientIDSetter.getNearlyTimeFromID(msgId).getTime() - 1000 * 60 * 5L;
            return mqAdminTemplate.execute(new MQAdminCallback<Result<MessageExt>>() {
                public Result<MessageExt> callback(MQAdminExt mqAdmin) throws Exception {
                    SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                    QueryResult queryResult = sohuMQAdmin.queryMessageByUniqKey(topic, msgId, 32, beginQueryTime, Long.MAX_VALUE);
                    if(queryResult.getMessageList() == null || queryResult.getMessageList().size() == 0){
                        return Result.getResult(Status.NO_RESULT);
                    }
                    return Result.getResult(queryResult.getMessageList().get(0));
                }

                public Result<MessageExt> exception(Exception e) throws Exception {
                    logger.error("queryMessage cluster:{} topic:{} msgId:{}", cluster, topic, msgId, e);
                    return Result.getWebErrorResult(e);
                }

                public Cluster mqCluster() {
                    return cluster;
                }
            });
    }

    /**
     * 查询Broker集群信息
     *
     * @param cluster
     * @return
     */
    public Result<ClusterInfo> examineBrokerClusterInfo(Cluster cluster) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<ClusterInfo>>() {
            public Result<ClusterInfo> callback(MQAdminExt mqAdmin) throws Exception {
                return Result.getResult(mqAdmin.examineBrokerClusterInfo());
            }

            public Result<ClusterInfo> exception(Exception e) throws Exception {
                logger.error("examineBrokerClusterInfo cluster:{}", cluster, e);
                return Result.getWebErrorResult(e);
            }

            public Cluster mqCluster() {
                return cluster;
            }
        });
    }

    public MessageTypeClassLoader getMessageTypeClassLoader() {
        return messageTypeClassLoader;
    }
}
