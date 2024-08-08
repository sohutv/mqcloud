package com.sohu.tv.mq.cloud.web.controller.param;

public class UpdateSendMsgRateLimitParam {
    // topic
    private String topic;
    // 限流
    private double topicLimitQps = -1;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public double getTopicLimitQps() {
        return topicLimitQps;
    }

    public void setTopicLimitQps(double topicLimitQps) {
        this.topicLimitQps = topicLimitQps;
    }
}
