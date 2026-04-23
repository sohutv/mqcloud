package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;

/**
 * http消费者配置
 *
 * @author yongfeigao
 * @date 2024年7月12日
 */
public class HttpConsumerConfig {
    // 拉取消息量
    private int maxPullSize;
    // 拉取超时时间，单位毫秒
    private int consumerPullTimeoutMillis;
    // 消费超时时间，单位毫秒
    private int consumeTimeoutInMillis;

    // 消费暂停
    private Boolean pause;
    // 消费限速
    private Boolean rateLimiterEnabled;
    private Double limitRate;

    private MessageModel messageModel;

    private boolean pop;

    public boolean isPop() {
        return pop;
    }

    public void setPop(boolean pop) {
        this.pop = pop;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public int getMaxPullSize() {
        return maxPullSize;
    }

    public void setMaxPullSize(int maxPullSize) {
        this.maxPullSize = maxPullSize;
    }

    public int getConsumerPullTimeoutMillis() {
        return consumerPullTimeoutMillis;
    }

    public int getConsumerPullTimeoutInSeconds() {
        return consumerPullTimeoutMillis / 1000;
    }

    public void setConsumerPullTimeoutMillis(int consumerPullTimeoutMillis) {
        this.consumerPullTimeoutMillis = consumerPullTimeoutMillis;
    }

    public int getConsumeTimeoutInMillis() {
        return consumeTimeoutInMillis;
    }

    public int getConsumeTimeoutInSeconds() {
        return consumeTimeoutInMillis / 1000;
    }

    public void setConsumeTimeoutInMillis(int consumeTimeoutInMillis) {
        this.consumeTimeoutInMillis = consumeTimeoutInMillis;
    }

    public Boolean getPause() {
        return pause;
    }

    public void setPause(Boolean pause) {
        this.pause = pause;
    }

    public Boolean getRateLimiterEnabled() {
        return rateLimiterEnabled;
    }

    public void setRateLimiterEnabled(Boolean rateLimiterEnabled) {
        this.rateLimiterEnabled = rateLimiterEnabled;
    }

    public Double getLimitRate() {
        return limitRate;
    }

    public void setLimitRate(Double limitRate) {
        this.limitRate = limitRate;
    }

    public Boolean getEnableRateLimit() {
        return rateLimiterEnabled;
    }

    public Double getPermitsPerSecond() {
        return limitRate;
    }

    @Override
    public String toString() {
        return "HttpConsumerConfig{" +
                "maxPullSize=" + maxPullSize +
                ", consumerPullTimeoutMillis=" + consumerPullTimeoutMillis +
                ", consumeTimeoutInMillis=" + consumeTimeoutInMillis +
                '}';
    }
}
