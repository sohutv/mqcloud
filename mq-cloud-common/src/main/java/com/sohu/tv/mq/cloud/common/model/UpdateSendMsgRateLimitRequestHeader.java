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
    // 是否禁用
    private Boolean disabled;
    // 默认限流
    private double defaultLimitQps = -1;
    // 重试消息限流
    private double sendMsgBackLimitQps = -1;
    // topic
    private String topic;
    // 限流
    private double topicLimitQps = -1;

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
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
                "disabled=" + disabled +
                ", defaultLimitQps=" + defaultLimitQps +
                ", sendMsgBackLimitQps=" + sendMsgBackLimitQps +
                ", topic='" + topic + '\'' +
                ", topicLimitQps=" + topicLimitQps +
                '}';
    }
}
