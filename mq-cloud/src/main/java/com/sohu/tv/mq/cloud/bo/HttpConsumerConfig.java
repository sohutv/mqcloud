package com.sohu.tv.mq.cloud.bo;

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

    @Override
    public String toString() {
        return "HttpConsumerConfig{" +
                "maxPullSize=" + maxPullSize +
                ", consumerPullTimeoutMillis=" + consumerPullTimeoutMillis +
                ", consumeTimeoutInMillis=" + consumeTimeoutInMillis +
                '}';
    }
}
