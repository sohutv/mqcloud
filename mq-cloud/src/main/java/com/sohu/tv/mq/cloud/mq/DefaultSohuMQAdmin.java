package com.sohu.tv.mq.cloud.mq;

import com.sohu.tv.mq.cloud.common.model.*;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.*;

import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

import static org.apache.rocketmq.remoting.protocol.RemotingSysResponseCode.SUCCESS;

/**
 * 搜狐mq admin 实现
 * 
 * @author yongfeigao
 * @date 2020年4月28日
 */
public class DefaultSohuMQAdmin extends SohuMQAdmin {

    private MQCloudConfigHelper mqCloudConfigHelper;

    public DefaultSohuMQAdmin(MQCloudConfigHelper mqCloudConfigHelper) {
        super();
        this.mqCloudConfigHelper = mqCloudConfigHelper;
    }

    public DefaultSohuMQAdmin(MQCloudConfigHelper mqCloudConfigHelper, RPCHook rpcHook, long timeoutMillis) {
        super(rpcHook, timeoutMillis);
        this.mqCloudConfigHelper = mqCloudConfigHelper;
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
        int code = mqCloudConfigHelper.isOldReqestCodeBroker(brokerAddr) ? RequestCode.GET_BROKER_STORE_STATS
                : RequestCode.GET_BROKER_STORE_STATS_V2;
        RemotingCommand request = RemotingCommand.createRequestCommand(code, requestHeader);
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

        int code = mqCloudConfigHelper.isOldReqestCodeBroker(brokerAddr) ? RequestCode.VIEW_MOMENT_STATS_DATA
                : RequestCode.VIEW_MOMENT_STATS_DATA_V2;
        RemotingCommand request = RemotingCommand.createRequestCommand(code, requestHeader);
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
        int code = mqCloudConfigHelper.isOldReqestCodeBroker(brokerAddr) ? RequestCode.VIEW_SEND_MESSAGE_RATE_LIMIT
                : RequestCode.VIEW_SEND_MESSAGE_RATE_LIMIT_V2;
        RemotingCommand request = RemotingCommand.createRequestCommand(code, null);
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
        int code = mqCloudConfigHelper.isOldReqestCodeBroker(brokerAddr) ? RequestCode.UPDATE_SEND_MESSAGE_RATE_LIMIT
                : RequestCode.UPDATE_SEND_MESSAGE_RATE_LIMIT_V2;
        RemotingCommand request = RemotingCommand.createRequestCommand(code, updateSendMsgRateLimitRequestHeader);
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

    public GetMetaDataResponseHeader getControllerMetaDataSohu(final String controllerAddress) throws Exception {
        final RemotingCommand request = RemotingCommand.createRequestCommand(1005, null);
        final RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient().invokeSync(controllerAddress, request, 3000);
        assert response != null;
        if (response.getCode() == SUCCESS) {
            return (GetMetaDataResponseHeader) response.decodeCommandCustomHeader(GetMetaDataResponseHeader.class);
        }
        throw new MQBrokerException(response.getCode(), response.getRemark());
    }

    /**
     * 获取时间轮统计信息
     * @param addr
     * @return
     * @throws Exception
     */
    public TimerMetricsSerializeWrapper getTimerWheelMetrics(final String addr)  throws Exception {
        RemotingCommand request = RemotingCommand.createRequestCommand(61, null);
        RemotingCommand response = getMQClientInstance().getMQClientAPIImpl().getRemotingClient().invokeSync(MixAll.brokerVIPChannel(true, addr), request, 3000);
        assert response != null;
        switch (response.getCode()) {
            case ResponseCode.SUCCESS: {
                return TimerMetricsSerializeWrapper.decode(response.getBody(), TimerMetricsSerializeWrapper.class);
            }
            default:
                break;
        }
        throw new MQBrokerException(response.getCode(), response.getRemark(), addr);
    }
}
