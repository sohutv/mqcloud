package com.sohu.tv.mq.cloud.common.model;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 查看瞬时数据请求头
 * 
 * @author yongfeigao
 * @date 2020年7月9日
 */
public class UpdateSendMsgRateLimitRequestHeader implements CommandCustomHeader {
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

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    @Override
    public String toString() {
        return "UpdateSendMsgRateLimitRequestHeader{" +
                ", topic='" + topic + '\'' +
                ", topicLimitQps=" + topicLimitQps +
                '}';
    }
}
