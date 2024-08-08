package com.sohu.tv.mq.cloud.common.model;

import java.util.List;

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
/**
 * broker限流数据
 * @author yongfeigao
 * @date 2022年2月22日
 */
public class BrokerRateLimitData extends RemotingSerializable {
    // topic实时数据
    private List<TopicRateLimit> topicRateLimitList;

    public List<TopicRateLimit> getTopicRateLimitList() {
        return topicRateLimitList;
    }

    public void setTopicRateLimitList(List<TopicRateLimit> topicRateLimitList) {
        this.topicRateLimitList = topicRateLimitList;
    }
}
