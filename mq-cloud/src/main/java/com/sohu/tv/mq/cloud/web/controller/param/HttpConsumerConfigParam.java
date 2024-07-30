package com.sohu.tv.mq.cloud.web.controller.param;

/**
 * http消费者配置
 *
 * @author yongfeigao
 * @date 2024年7月12日
 */
public class HttpConsumerConfigParam {
    private long consumerId;
    private Integer pullSize;
    private Long pullTimeout;
    private Long consumeTimeout;

    public long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(long consumerId) {
        this.consumerId = consumerId;
    }

    public Integer getPullSize() {
        return pullSize;
    }

    public void setPullSize(Integer pullSize) {
        this.pullSize = pullSize;
    }

    public Long getPullTimeout() {
        return pullTimeout;
    }

    public void setPullTimeout(Long pullTimeout) {
        this.pullTimeout = pullTimeout;
    }

    public Long getConsumeTimeout() {
        return consumeTimeout;
    }

    public void setConsumeTimeout(Long consumeTimeout) {
        this.consumeTimeout = consumeTimeout;
    }

    public void reset() {
        if (pullTimeout != null) {
            pullTimeout = pullTimeout * 1000;
        }
        if (consumeTimeout != null) {
            consumeTimeout = consumeTimeout * 1000;
        }
    }
}
