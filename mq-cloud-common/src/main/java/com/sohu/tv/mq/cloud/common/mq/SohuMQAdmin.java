package com.sohu.tv.mq.cloud.common.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.sohu.tv.mq.cloud.common.model.BrokerMomentStatsData;
import com.sohu.tv.mq.cloud.common.model.BrokerRateLimitData;
import com.sohu.tv.mq.cloud.common.model.*;
import com.sohu.tv.mq.util.Constant;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RemotingSysResponseCode;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.body.*;
import org.apache.rocketmq.remoting.protocol.header.ConsumeMessageDirectlyResultRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.GetConsumerRunningInfoRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.ResetOffsetRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.namesrv.UnRegisterBrokerRequestHeader;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExtImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
            return _getConsumerRunningInfo(consumerGroup, clientId, jstack);
        }
        // 兼容proxy模式
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
     * 兼容c++数据
     *
     * @param consumerGroup
     * @param clientId
     * @param jstack
     * @return
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     */
    private ConsumerRunningInfo _getConsumerRunningInfo(String consumerGroup, String clientId, boolean jstack) throws RemotingException, MQClientException, InterruptedException {
        String topic = MixAll.RETRY_GROUP_TOPIC_PREFIX + consumerGroup;
        TopicRouteData topicRouteData = examineTopicRouteInfo(topic);
        List<BrokerData> brokerDatas = topicRouteData.getBrokerDatas();
        if (brokerDatas == null || brokerDatas.size() == 0) {
            throw new MQClientException(0, "route is empty");
        }
        // 随机选择一个broker
        Collections.shuffle(brokerDatas);
        String addr = null;
        for (BrokerData brokerData : brokerDatas) {
            addr = brokerData.selectBrokerAddr();
            if (addr != null) {
                break;
            }
        }
        if (addr == null) {
            throw new MQClientException(1, "broker addr is null");
        }
        MQClientInstance mqClientInstance = null;
        long timeoutMillis = 0;
        try {
            mqClientInstance = getMQClientInstance();
            timeoutMillis = getTimeoutMillis();
        } catch (Exception e) {
            throw new MQClientException("getMQClientInstance exception", e);
        }
        GetConsumerRunningInfoRequestHeader requestHeader = new GetConsumerRunningInfoRequestHeader();
        requestHeader.setConsumerGroup(consumerGroup);
        requestHeader.setClientId(clientId);
        requestHeader.setJstackEnable(jstack);
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.GET_CONSUMER_RUNNING_INFO, requestHeader);
        RemotingCommand response = mqClientInstance.getMQClientAPIImpl().getRemotingClient().invokeSync(
                MixAll.brokerVIPChannel(mqClientInstance.getClientConfig().isVipChannelEnabled(), addr), request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
            case ResponseCode.SUCCESS: {
                byte[] body = response.getBody();
                if (body != null) {
                    Object obj = JSON.parse(body);
                    if (obj instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) obj;
                        JSONObject mqs = jsonObject.getJSONObject("mqTable");
                        if (mqs != null) {
                            for (Map.Entry<String, Object> entry : mqs.entrySet()) {
                                JSONObject processQueueInfo = (JSONObject) entry.getValue();
                                try {
                                    processQueueInfo.getLong("commitOffset");
                                } catch (JSONException e) {
                                    // commitOffset超出long,删除
                                    processQueueInfo.remove("commitOffset");
                                }
                            }
                        }
                        return jsonObject.toJavaObject(ConsumerRunningInfo.class);
                    }
                }
            }
            default:
                break;
        }
        throw new MQClientException(response.getCode(), response.getRemark());
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
}
