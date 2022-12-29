package com.sohu.tv.mq.cloud.common.mq;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.protocol.RequestCode;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.common.protocol.header.GetConsumerRunningInfoRequestHeader;
import org.apache.rocketmq.common.protocol.header.namesrv.UnRegisterBrokerRequestHeader;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExtImpl;

import com.sohu.tv.mq.cloud.common.model.BrokerMomentStatsData;
import com.sohu.tv.mq.cloud.common.model.BrokerRateLimitData;
import com.sohu.tv.mq.cloud.common.model.BrokerStoreStat;
import com.sohu.tv.mq.cloud.common.model.UpdateSendMsgRateLimitRequestHeader;
import com.sohu.tv.mq.util.Constant;

/**
 * sohu实现，为了添加扩展某些方法
 * 
 * @author yongfeigao
 * @date 2018年10月16日
 */
public abstract class SohuMQAdmin extends DefaultMQAdminExt {

    public SohuMQAdmin() {
        super();
    }

    public SohuMQAdmin(RPCHook rpcHook, long timeoutMillis) {
        super(rpcHook, timeoutMillis);
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
}
