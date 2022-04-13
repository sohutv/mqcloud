package com.sohu.tv.mq.cloud.common.model;

import java.util.List;

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
/**
 * broker限流数据
 * @author yongfeigao
 * @date 2022年2月22日
 */
public class BrokerRateLimitData extends RemotingSerializable {
    // 是否禁用
    private boolean disabled;
    // 默认限流
    private double defaultLimitQps;
    // 重试消息限流
    private double sendMsgBackLimitQps;
    // topic实时数据
    private List<TopicRateLimit> topicRateLimitList;

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public double getDefaultLimitQps() {
        return defaultLimitQps;
    }

    public void setDefaultLimitQps(double defaultLimitQps) {
        this.defaultLimitQps = defaultLimitQps;
    }

    public double getSendMsgBackLimitQps() {
        return sendMsgBackLimitQps;
    }

    public void setSendMsgBackLimitQps(double sendMsgBackLimitQps) {
        this.sendMsgBackLimitQps = sendMsgBackLimitQps;
    }

    public List<TopicRateLimit> getTopicRateLimitList() {
        return topicRateLimitList;
    }

    public void setTopicRateLimitList(List<TopicRateLimit> topicRateLimitList) {
        this.topicRateLimitList = topicRateLimitList;
    }
}
