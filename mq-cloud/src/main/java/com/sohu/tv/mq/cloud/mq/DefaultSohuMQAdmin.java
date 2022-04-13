package com.sohu.tv.mq.cloud.mq;

import com.sohu.tv.mq.cloud.common.model.*;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;

import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 搜狐mq admin 实现
 * 
 * @author yongfeigao
 * @date 2020年4月28日
 */
public class DefaultSohuMQAdmin extends SohuMQAdmin {

    public DefaultSohuMQAdmin() {
        super();
    }

    public DefaultSohuMQAdmin(RPCHook rpcHook, long timeoutMillis) {
        super(rpcHook, timeoutMillis);
    }

    public BrokerStoreStat getBrokerStoreStats(String brokerAddr) throws Exception {
        PercentileStat percentileStat = fetchStoreStatsInBroker(brokerAddr);
        if (percentileStat == null) {
            return null;
        }
        BrokerStoreStat brokerStoreStat = new BrokerStoreStat();
        brokerStoreStat.setAvg(percentileStat.getAvg());
        brokerStoreStat.setCount(percentileStat.getCount());
        brokerStoreStat.setStatTime(percentileStat.getStatTime());
        brokerStoreStat.setMax(percentileStat.getMax());
        brokerStoreStat.setPercent99(percentileStat.getPercent99());
        brokerStoreStat.setPercent90(percentileStat.getPercent90());
        return brokerStoreStat;
    }

    public PercentileStat fetchStoreStatsInBroker(final String brokerAddr) throws Exception {
        GetBrokerStoreStatRequestHeader requestHeader = new GetBrokerStoreStatRequestHeader();
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.GET_BROKER_STORE_STATS,
                requestHeader);
        RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient()
                .invokeSync(MixAll.brokerVIPChannel(isVipChannelEnabled(), brokerAddr), request, getTimeoutMillis());
        assert response != null;
        switch (response.getCode()) {
            case ResponseCode.SUCCESS: {
                byte[] body = response.getBody();
                if (body != null) {
                    return PercentileStat.decode(body, PercentileStat.class);
                }
            }
            default:
                break;
        }
        throw new MQClientException(response.getCode(), response.getRemark());
    }

    @Override
    public BrokerMomentStatsData getMomentStatsInBroker(String brokerAddr, String statsName, long minValue)
            throws Exception {
        ViewMomentStatsDataRequestHeader requestHeader = new ViewMomentStatsDataRequestHeader();
        requestHeader.setStatsName(statsName);
        requestHeader.setMinValue(minValue);

        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.VIEW_MOMENT_STATS_DATA,
                requestHeader);

        RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient()
                .invokeSync(MixAll.brokerVIPChannel(isVipChannelEnabled(), brokerAddr), request, getTimeoutMillis());
        assert response != null;
        switch (response.getCode()) {
            case ResponseCode.SUCCESS: {
                byte[] body = response.getBody();
                if (body != null) {
                    return BrokerMomentStatsData.decode(body, BrokerMomentStatsData.class);
                }
                return null;
            }
            default:
                break;
        }
        throw new MQClientException(response.getCode(), response.getRemark());
    }

    @Override
    public BrokerRateLimitData fetchSendMessageRateLimitInBroker(String brokerAddr) throws Exception {
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.VIEW_SEND_MESSAGE_RATE_LIMIT, null);
        RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient()
                .invokeSync(MixAll.brokerVIPChannel(isVipChannelEnabled(), brokerAddr), request, getTimeoutMillis());
        assert response != null;
        switch (response.getCode()) {
            case ResponseCode.SUCCESS: {
                byte[] body = response.getBody();
                if (body != null) {
                    return BrokerRateLimitData.decode(body, BrokerRateLimitData.class);
                }
                return null;
            }
            default:
                break;
        }
        throw new MQClientException(response.getCode(), response.getRemark());
    }

    @Override
    public void updateSendMessageRateLimit(String brokerAddr,
                                           UpdateSendMsgRateLimitRequestHeader updateSendMsgRateLimitRequestHeader) throws Exception {
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.UPDATE_SEND_MESSAGE_RATE_LIMIT,
                updateSendMsgRateLimitRequestHeader);
        RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient()
                .invokeSync(MixAll.brokerVIPChannel(isVipChannelEnabled(), brokerAddr), request, getTimeoutMillis());
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
