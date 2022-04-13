package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.common.model.BrokerMomentStatsData;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.protocol.RemotingSysResponseCode;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消费落后查询
 * 
 * @author yongfeigao
 * @date 2020年7月10日
 */
@Service
public class ConsumeFallBehindService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    /**
     * 获取消费落后大小
     * 
     * @param brokerAddr
     * @param cluster
     * @param minValue
     * @return
     */
    public Result<BrokerMomentStatsData> getConsumeFallBehindSize(String brokerAddr, Cluster cluster, long minValue) {
        return getConsumeFallBehindData(brokerAddr, cluster, "GROUP_GET_FALL_SIZE", minValue);
    }

    /**
     * 获取消费落后时间
     * 
     * @param brokerAddr
     * @param cluster
     * @param minValue
     * @return
     */
    public Result<BrokerMomentStatsData> getConsumeFallBehindTime(String brokerAddr, Cluster cluster, long minValue) {
        return getConsumeFallBehindData(brokerAddr, cluster, "GROUP_GET_FALL_TIME", minValue);
    }

    /**
     * 获取消费落后数据
     * 
     * @param brokerAddr
     * @param cluster
     * @param statsName
     * @param minValue
     * @return
     */
    private Result<BrokerMomentStatsData> getConsumeFallBehindData(String brokerAddr, Cluster cluster, String statsName,
            long minValue) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<BrokerMomentStatsData>>() {
            public Result<BrokerMomentStatsData> callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                return Result.getResult(sohuMQAdmin.getMomentStatsInBroker(brokerAddr, statsName, minValue));
            }

            public Cluster mqCluster() {
                return cluster;
            }

            @Override
            public Result<BrokerMomentStatsData> exception(Exception e) throws Exception {
                logger.warn("getConsumeFallBehindData, cluster:{}, brokerAddr:{}, statsName:{}, minValue:{} error:{}",
                        cluster, brokerAddr, statsName, minValue, e.toString());
                // 判断是否支持
                if (e instanceof MQClientException && ((MQClientException) e).getResponseCode() == RemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED) {
                    Result<BrokerMomentStatsData> result = Result.getResult(Status.BROKER_UNSUPPORTED_ERROR);
                    return result.setException(e);
                }
                return Result.getDBErrorResult(e);
            }
        });
    }
}
