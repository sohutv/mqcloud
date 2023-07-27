package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.remoting.protocol.body.BrokerStatsData;

/**
 * topic小时流量
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月26日
 */
public class TopicHourTraffic extends TopicTraffic {

    @Override
    public void addCount(BrokerStatsData brokerPutStatsData) {
        super.addCount(brokerPutStatsData.getStatsHour().getSum());
    }

    @Override
    public void addSize(BrokerStatsData brokerSizeStatsData) {
        super.addSize(brokerSizeStatsData.getStatsHour().getSum());
    }
}
