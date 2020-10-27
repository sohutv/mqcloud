package com.sohu.tv.mq.cloud.mq;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;

import com.sohu.tv.mq.cloud.common.model.BrokerMomentStatsData;
import com.sohu.tv.mq.cloud.common.model.BrokerStoreStat;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
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

    @Override
    public BrokerStoreStat getBrokerStoreStats(String brokerAddr) throws RemotingConnectException,
            RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BrokerMomentStatsData getMomentStatsInBroker(String brokerAddr, String statsName, long minValue)
            throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException,
            InterruptedException {
        throw new UnsupportedOperationException();
    }
}
