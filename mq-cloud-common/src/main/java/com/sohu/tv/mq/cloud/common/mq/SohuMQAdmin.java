package com.sohu.tv.mq.cloud.common.mq;

import com.sohu.tv.mq.cloud.common.model.BrokerMomentStatsData;
import com.sohu.tv.mq.cloud.common.model.BrokerRateLimitData;
import com.sohu.tv.mq.cloud.common.model.*;
import com.sohu.tv.mq.cloud.common.model.UpdateSendMsgRateLimitRequestHeader;
import com.sohu.tv.mq.util.Constant;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.*;
import org.apache.rocketmq.remoting.InvokeCallback;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.*;
import org.apache.rocketmq.remoting.protocol.*;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.body.*;
import org.apache.rocketmq.remoting.protocol.header.*;
import org.apache.rocketmq.remoting.protocol.header.namesrv.UnRegisterBrokerRequestHeader;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExtImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * sohu实现，为了添加扩展某些方法
 * 
 * @author yongfeigao
 * @date 2018年10月16日
 */
public abstract class SohuMQAdmin extends DefaultMQAdminExt {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    // 是否使用代理
    private boolean proxyEnabled;

    public SohuMQAdmin() {
        super();
    }

    public SohuMQAdmin(RPCHook rpcHook, long timeoutMillis) {
        super(rpcHook, timeoutMillis);
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public void setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    /**
     * 获取系统内置topic
     * 
     * @param timeoutMillis
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     */
    public TopicList getSystemTopicList(long timeoutMillis)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
            RemotingException, MQClientException, InterruptedException {
        return getMQClientInstance().getMQClientAPIImpl().getSystemTopicList(timeoutMillis);
    }

    /**
     * 发送消息
     * 
     * @param msg
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws MQClientException
     * @throws RemotingException
     * @throws MQBrokerException
     * @throws InterruptedException
     */
    public SendResult sendMessage(Message msg) throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return getMQClientInstance().getDefaultMQProducer().send(msg);
    }

    /**
     * 发送消息
     *
     * @param msg
     * @param messageQueue
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws MQClientException
     * @throws RemotingException
     * @throws MQBrokerException
     * @throws InterruptedException
     */
    public SendResult sendMessage(Message msg, MessageQueue messageQueue) throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return getMQClientInstance().getDefaultMQProducer().send(msg, messageQueue);
    }

    /**
     * 获取 @MQClientInstance
     * 
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public MQClientInstance getMQClientInstance()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        // 反射获取defaultMQAdminExtImpl实例
        Field defaultMQAdminExtImplField = DefaultMQAdminExt.class.getDeclaredField("defaultMQAdminExtImpl");
        defaultMQAdminExtImplField.setAccessible(true);
        DefaultMQAdminExtImpl defaultMQAdminExtImpl = (DefaultMQAdminExtImpl) defaultMQAdminExtImplField.get(this);
        // 反射获取mqClientInstance实例
        Field field = DefaultMQAdminExtImpl.class.getDeclaredField("mqClientInstance");
        field.setAccessible(true);
        MQClientInstance mqClientInstance = (MQClientInstance) field.get(defaultMQAdminExtImpl);
        return mqClientInstance;
    }
    
    public long getTimeoutMillis() throws Exception {
        // 反射获取defaultMQAdminExtImpl实例
        Field defaultMQAdminExtImplField = DefaultMQAdminExt.class.getDeclaredField("defaultMQAdminExtImpl");
        defaultMQAdminExtImplField.setAccessible(true);
        DefaultMQAdminExtImpl defaultMQAdminExtImpl = (DefaultMQAdminExtImpl) defaultMQAdminExtImplField.get(this);
        // 反射获取mqClientInstance实例
        Field field = DefaultMQAdminExtImpl.class.getDeclaredField("timeoutMillis");
        field.setAccessible(true);
        return (long) field.get(defaultMQAdminExtImpl);
    }

    /**
     * 获取broker存储统计
     * 
     * @param brokerAddr
     * @return
     * @throws RemotingConnectException
     * @throws RemotingSendRequestException
     * @throws RemotingTimeoutException
     * @throws MQClientException
     * @throws InterruptedException
     */
    public abstract BrokerStoreStat getBrokerStoreStats(String brokerAddr) throws Exception;

    /**
     * 从broker获取瞬时统计
     * 
     * @param brokerAddr
     * @param statsName
     * @param minValue
     * @return
     * @throws RemotingConnectException
     * @throws RemotingSendRequestException
     * @throws RemotingTimeoutException
     * @throws MQClientException
     * @throws InterruptedException
     */
    public abstract BrokerMomentStatsData getMomentStatsInBroker(String brokerAddr, String statsName, long minValue) throws Exception;

    /**
     * 获取消费线程指标
     * 
     * @param addr
     * @param consumerGroup
     * @param clientId
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public ConsumerRunningInfo getConsumeThreadMetrics(String consumerGroup, String clientId,
            final long timeoutMillis) throws RemotingException, MQClientException, InterruptedException,
            NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        return getConsumeMetrics(consumerGroup, clientId, Constant.COMMAND_THREAD_METRIC, timeoutMillis);
    }

    /**
     * 获取消费失败指标
     * 
     * @param addr
     * @param consumerGroup
     * @param clientId
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public ConsumerRunningInfo getConsumeFailedMetrics(String consumerGroup, String clientId,
            final long timeoutMillis) throws RemotingException, MQClientException, InterruptedException,
            NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        return getConsumeMetrics(consumerGroup, clientId, Constant.COMMAND_FAILED_METRIC, timeoutMillis);
    }

    /**
     * 获取消费指标
     * 
     * @param addr
     * @param consumerGroup
     * @param clientId
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public ConsumerRunningInfo getConsumeMetrics(String consumerGroup, String clientId, String command,
            final long timeoutMillis) throws RemotingException, MQClientException, InterruptedException,
            NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (isProxyEnabled()) {
            return getConsumeMetricsOfProxy(consumerGroup, clientId, command, timeoutMillis);
        }
        return _getConsumeMetrics(consumerGroup, clientId, command, timeoutMillis);
    }

    /**
     * 获取消费指标
     *
     * @param addr
     * @param consumerGroup
     * @param clientId
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public ConsumerRunningInfo getConsumeMetricsOfProxy(String consumerGroup, String clientId, String command,
                                                 final long timeoutMillis) throws RemotingException, MQClientException, InterruptedException,
            NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        GetConsumerRunningInfoRequestHeader requestHeader = new GetConsumerRunningInfoRequestHeader();
        requestHeader.setConsumerGroup(consumerGroup);
        requestHeader.setClientId(clientId);
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.GET_CONSUMER_RUNNING_INFO, requestHeader);
        request.addExtField(command, Constant.COMMAND_TRUE);
        for (String addr : getNameServerAddressList()) {
            RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient()
                    .invokeSync(MixAll.brokerVIPChannel(isVipChannelEnabled(), addr), request, timeoutMillis);
            assert response != null;
            switch (response.getCode()) {
                case ResponseCode.SUCCESS: {
                    byte[] body = response.getBody();
                    if (body != null) {
                        ConsumerRunningInfo info = ConsumerRunningInfo.decode(body, ConsumerRunningInfo.class);
                        return info;
                    }
                }
                case ResponseCode.REQUEST_CODE_NOT_SUPPORTED:
                    continue;
                default:
                    break;
            }
            throw new MQClientException(response.getCode(), response.getRemark());
        }
        return null;
    }

    /**
     * 获取消费指标
     *
     * @param addr
     * @param consumerGroup
     * @param clientId
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public ConsumerRunningInfo _getConsumeMetrics(String consumerGroup, String clientId, String command,
                                                 final long timeoutMillis) throws RemotingException, MQClientException, InterruptedException,
            NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String topic = MixAll.RETRY_GROUP_TOPIC_PREFIX + consumerGroup;
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(topic);
        List<BrokerData> brokerDatas = topicRouteData.getBrokerDatas();
        if (brokerDatas != null) {
            for (BrokerData brokerData : brokerDatas) {
                String addr = brokerData.selectBrokerAddr();
                if (addr != null) {
                    GetConsumerRunningInfoRequestHeader requestHeader = new GetConsumerRunningInfoRequestHeader();
                    requestHeader.setConsumerGroup(consumerGroup);
                    requestHeader.setClientId(clientId);
                    RemotingCommand request = RemotingCommand.createRequestCommand(
                            RequestCode.GET_CONSUMER_RUNNING_INFO, requestHeader);
                    request.addExtField(command, Constant.COMMAND_TRUE);
                    RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient()
                            .invokeSync(MixAll.brokerVIPChannel(isVipChannelEnabled(), addr), request, timeoutMillis);
                    assert response != null;
                    switch (response.getCode()) {
                        case ResponseCode.SUCCESS: {
                            byte[] body = response.getBody();
                            if (body != null) {
                                ConsumerRunningInfo info = ConsumerRunningInfo.decode(body, ConsumerRunningInfo.class);
                                return info;
                            }
                        }
                        default:
                            break;
                    }
                    throw new MQClientException(response.getCode(), response.getRemark());
                }
            }
        }
        return null;
    }

    /**
     * 消费某个时间段的消息
     * 
     * @param topic
     * @param group
     * @param startTimestamp
     * @param endTimestamp
     * @param timeoutMillis
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void consumeTimespanMessage(String clientId, String topic, String group, long startTimestamp,
            long endTimestamp) throws Exception {
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(topic);
        List<BrokerData> brokerDatas = topicRouteData.getBrokerDatas();
        if (brokerDatas != null) {
            for (BrokerData brokerData : brokerDatas) {
                String addr = brokerData.selectBrokerAddr();
                if (addr == null) {
                    continue;
                }
                GetConsumerRunningInfoRequestHeader requestHeader = new GetConsumerRunningInfoRequestHeader();
                requestHeader.setConsumerGroup(group);
                requestHeader.setClientId(clientId);
                RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.GET_CONSUMER_RUNNING_INFO,
                        requestHeader);
                request.addExtField(Constant.COMMAND_TIMESPAN_TOPIC, topic);
                request.addExtField(Constant.COMMAND_TIMESPAN_START, String.valueOf(startTimestamp));
                request.addExtField(Constant.COMMAND_TIMESPAN_END, String.valueOf(endTimestamp));
                RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient().invokeSync(
                        MixAll.brokerVIPChannel(isVipChannelEnabled(), addr), request, getTimeoutMillis());
                assert response != null;
                switch (response.getCode()) {
                    case ResponseCode.SUCCESS: {
                        return;
                    }
                    default:
                        break;
                }
                throw new MQClientException(response.getCode(), response.getRemark());
            }
        }
    }
    
    public abstract BrokerRateLimitData fetchSendMessageRateLimitInBroker(String brokerAddr) throws Exception;

    public abstract void updateSendMessageRateLimit(String brokerAddr,
            UpdateSendMsgRateLimitRequestHeader updateSendMsgRateLimitRequestHeader) throws Exception;


    public void unregisterBroker(
            final String namesrvAddr,
            final String clusterName,
            final String brokerAddr,
            final String brokerName,
            final long brokerId
    ) throws Exception {
        UnRegisterBrokerRequestHeader requestHeader = new UnRegisterBrokerRequestHeader();
        requestHeader.setBrokerAddr(brokerAddr);
        requestHeader.setBrokerId(brokerId);
        requestHeader.setBrokerName(brokerName);
        requestHeader.setClusterName(clusterName);
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.UNREGISTER_BROKER, requestHeader);

        RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient().invokeSync(namesrvAddr, request, 3000);
        assert response != null;
        switch (response.getCode()) {
            case ResponseCode.SUCCESS: {
                return;
            }
            default:
                break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark(), brokerAddr);
    }

    /**
     * 获取controller元数据：为了兼容4.x & 5.x
     * @param controllerAddress
     * @return
     * @throws Exception
     */
    public abstract GetMetaDataResponseHeader getControllerMetaDataSohu(final String controllerAddress) throws Exception;

    /**
     * 获取定时消息时间轮指标
     * @param addr
     * @return
     * @throws Exception
     */
    public abstract TimerMetricsSerializeWrapper getTimerWheelMetrics(final String addr)  throws Exception;

    /**
     * 是否存活
     * @return
     */
    public boolean isAlive() throws Exception {
        // 兼容proxy模式
        if (isProxyEnabled()) {
            RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.CHECK_CLIENT_CONFIG, null);
            RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient().invokeSync(null, request, getTimeoutMillis());
            assert response != null;
            if (ResponseCode.SUCCESS != response.getCode()) {
                throw new MQClientException(response.getCode(), response.getRemark());
            }
            return true;
        }
        ClusterInfo clusterInfo = examineBrokerClusterInfo();
        if (clusterInfo == null) {
            return false;
        }
        if (clusterInfo.getBrokerAddrTable() == null) {
            return false;
        }
        if (clusterInfo.getBrokerAddrTable().size() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * proxy-remoting协议重置offset
     *
     * @param topic
     * @param group
     * @param timestamp
     * @param offsetTable
     * @throws Exception
     */
    public void resetOffsetOfProxy(String topic, String group, long timestamp, Map<MessageQueue, Long> offsetTable) throws Exception {
        ResetOffsetRequestHeader requestHeader = new ResetOffsetRequestHeader();
        requestHeader.setTopic(topic);
        requestHeader.setGroup(group);
        requestHeader.setTimestamp(timestamp);
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.RESET_CONSUMER_CLIENT_OFFSET, requestHeader);
        ResetOffsetBody body = new ResetOffsetBody();
        body.setOffsetTable(offsetTable);
        request.setBody(body.encode());
        // 重置offset只需要找一个proxy即可
        String address = getNameServerAddressList().get(0);
        try {
            getMQClientInstance().getMQClientAPIImpl().getRemotingClient().invokeOneway(address, request, getTimeoutMillis());
            logger.info("resetOffset success, address:{}, topic:{}, group:{} timestamp:{}", address, topic, group, timestamp);
        } catch (Exception e) {
            logger.error("resetOffset error, address:{}, topic:{}, group:{} timestamp:{}", address, topic, group, timestamp, e);
            throw e;
        }
    }

    /**
     * proxy-remoting协议直接消费消息
     *
     * @param topic
     * @param consumerGroup
     * @param clientId
     * @param msgId
     * @return
     * @throws Exception
     */
    public ConsumeMessageDirectlyResult consumeMessageDirectlyOfProxy(String topic, String consumerGroup,
                                                                      String clientId, String msgId, MessageExt messageExt) throws Exception {
        ConsumeMessageDirectlyResultRequestHeader requestHeader = new ConsumeMessageDirectlyResultRequestHeader();
        requestHeader.setTopic(topic);
        requestHeader.setConsumerGroup(consumerGroup);
        requestHeader.setClientId(clientId);
        requestHeader.setMsgId(msgId);
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.CONSUME_MESSAGE_DIRECTLY, requestHeader);
        request.addExtField("brokerName", messageExt.getBrokerName());
        request.setBody(MessageDecoder.encode(messageExt, false));
        String address = getNameServerAddressList().get(0);
        RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient().invokeSync(address, request, getTimeoutMillis());
        assert response != null;
        switch (response.getCode()) {
            case ResponseCode.SUCCESS: {
                byte[] body = response.getBody();
                if (body != null) {
                    ConsumeMessageDirectlyResult info = ConsumeMessageDirectlyResult.decode(body, ConsumeMessageDirectlyResult.class);
                    return info;
                }
            }
            default:
                break;
        }
        throw new MQClientException(response.getCode(), response.getRemark());
    }

    @Override
    public ConsumerRunningInfo getConsumerRunningInfo(String consumerGroup, String clientId, boolean jstack) throws RemotingException, MQClientException, InterruptedException {
        if (!proxyEnabled) {
            return super.getConsumerRunningInfo(consumerGroup, clientId, jstack);
        }
        for (String addr : getNameServerAddressList()) {
            try {
                ConsumerRunningInfo consumerRunningInfo = getMQClientInstance().getMQClientAPIImpl()
                        .getConsumerRunningInfo(addr, consumerGroup, clientId, jstack, getTimeoutMillis());
                return consumerRunningInfo;
            } catch (MQClientException e) {
                if (e.getResponseCode() == RemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED) {
                    continue;
                }
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * 设置路由参数
     *
     * @throws Exception
     */
    public void setProtocolInner(int protocol) {
        // 设置路由参数
        try {
            Field protocolField = ClientConfig.class.getDeclaredField("protocol");
            protocolField.setAccessible(true);
            int prevProtocol = protocolField.getInt(this);
            // 如果之前没有设置过protocol，才设置
            if (prevProtocol == 0) {
                protocolField.set(this, protocol);
            }
        } catch (Exception e) {
            logger.warn("setProtocol error:{}", e.toString());
        }
    }

    /**
     * 查询时间轮定时消息
     *
     * @throws Exception
     */
    public QueryResult queryTimerMessageByUniqKey(String broker, String uniqKey,
                                                  long begin, long end,
                                                  boolean isUniqKey) throws Exception {
        long nowTime = System.currentTimeMillis();
        begin = begin == 0L ? nowTime - 30 * 24 * 60 * 60 * 1000L : begin;
        end = end == 0L ? nowTime : end;
        final String timerTopic = "rmq_sys_wheel_timer";
        MQClientInstance mqClientInstance = getMQClientInstance();
        TopicRouteData topicRouteData = mqClientInstance.getAnExistTopicRouteData(timerTopic);
        if (null == topicRouteData) {
            mqClientInstance.updateTopicRouteInfoFromNameServer(timerTopic);
            topicRouteData = mqClientInstance.getAnExistTopicRouteData(timerTopic);
        }
        if (topicRouteData != null) {
            String targetBroker = topicRouteData.getBrokerDatas().stream()
                    .filter(t -> broker.equals(t.getBrokerName()))
                    .map(BrokerData::selectBrokerAddr)
                    .findFirst()
                    .orElse(null);
            if (targetBroker != null) {
                final List<QueryResult> queryResultList = new LinkedList<>();
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                try {
                    QueryMessageRequestHeader requestHeader = new QueryMessageRequestHeader();
                    requestHeader.setTopic(timerTopic);
                    requestHeader.setKey(uniqKey);
                    requestHeader.setMaxNum(64);
                    requestHeader.setBeginTimestamp(begin);
                    requestHeader.setEndTimestamp(end);
                    mqClientInstance.getMQClientAPIImpl().queryMessage(targetBroker, requestHeader, 10000,
                            (InvokeCallback) responseFuture -> {
                                try {
                                    RemotingCommand response = responseFuture.getResponseCommand();
                                    if (response != null) {
                                        switch (response.getCode()) {
                                            case ResponseCode.SUCCESS: {
                                                QueryMessageResponseHeader responseHeader = null;
                                                try {
                                                    responseHeader =
                                                            (QueryMessageResponseHeader) response
                                                                    .decodeCommandCustomHeader(QueryMessageResponseHeader.class);
                                                } catch (RemotingCommandException e) {
                                                    logger.error("decodeCommandCustomHeader exception", e);
                                                    return;
                                                }

                                                List<MessageExt> wrappers =
                                                        MessageDecoder.decodes(ByteBuffer.wrap(response.getBody()), true);

                                                QueryResult qr = new QueryResult(responseHeader.getIndexLastUpdateTimestamp(), wrappers);
                                                queryResultList.add(qr);
                                                break;
                                            }
                                            default:
                                                logger.warn("getResponseCommand failed, {} {}", response.getCode(), response.getRemark());
                                                break;
                                        }
                                    } else {
                                        logger.warn("getResponseCommand return null");
                                    }
                                } finally {
                                    countDownLatch.countDown();
                                }
                            }, isUniqKey);
                } catch (Exception e) {
                    logger.warn("queryMessage exception", e);
                }
                boolean ok = countDownLatch.await(15000, TimeUnit.MILLISECONDS);
                if (!ok) {
                    logger.warn("queryMessage, maybe some broker failed");
                }
                long indexLastUpdateTimestamp = 0;
                List<MessageExt> messageList = new LinkedList<>();
                for (QueryResult qr : queryResultList) {
                    if (qr.getIndexLastUpdateTimestamp() > indexLastUpdateTimestamp) {
                        indexLastUpdateTimestamp = qr.getIndexLastUpdateTimestamp();
                    }
                    for (MessageExt msgExt : qr.getMessageList()) {
                        if (isUniqKey) {
                            if (msgExt.getMsgId().equals(uniqKey)) {
                                messageList.add(msgExt);
                            } else {
                                logger.warn("queryMessage by uniqKey, find message key not matched, maybe hash duplicate {}", msgExt.toString());
                            }
                        } else {
                            String keys = msgExt.getKeys();
                            String msgTopic = msgExt.getTopic();
                            if (keys != null) {
                                boolean matched = false;
                                String[] keyArray = keys.split(MessageConst.KEY_SEPARATOR);
                                for (String k : keyArray) {
                                    // both topic and key must be equal at the same time
                                    if (Objects.equals(uniqKey, k) && Objects.equals(timerTopic, msgTopic)) {
                                        matched = true;
                                        break;
                                    }
                                }

                                if (matched) {
                                    messageList.add(msgExt);
                                } else {
                                    logger.warn("queryMessage, find message key not matched, maybe hash duplicate {}", msgExt.toString());
                                }
                            }
                        }
                    }
                }

                //If namespace not null , reset Topic without namespace.
                if (null != mqClientInstance.getClientConfig().getNamespace()) {
                    for (MessageExt messageExt : messageList) {
                        messageExt.setTopic(NamespaceUtil.withoutNamespace(messageExt.getTopic(), mqClientInstance.getClientConfig().getNamespace()));
                    }
                }

                if (!messageList.isEmpty()) {
                    return new QueryResult(indexLastUpdateTimestamp, messageList);
                } else {
                    throw new MQClientException(ResponseCode.NO_MESSAGE, "query message by key finished, but no message.");
                }
            }
        }
        throw new MQClientException(ResponseCode.TOPIC_NOT_EXIST, "The topic[" + timerTopic + "] not matched route info, broker[" + broker + "]");
    }
}
