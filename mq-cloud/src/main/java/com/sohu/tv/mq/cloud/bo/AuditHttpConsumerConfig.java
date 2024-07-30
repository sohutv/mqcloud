package com.sohu.tv.mq.cloud.bo;

/**
 * http消费者配置审核
 *
 * @author yongfeigao
 * @date 2024年7月12日
 */
public class AuditHttpConsumerConfig {
    // 审核id
    private long aid;
    // 消费者id
    private long consumerId;
    // 拉取消息量
    private Integer pullSize;
    // 拉取超时时间，单位毫秒
    private Long pullTimeout;
    // 消费超时时间，单位毫秒
    private Long consumeTimeout;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

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

    @Override
    public String toString() {
        return "AuditHttpConsumerConfig{" +
                "aid=" + aid +
                ", consumerId=" + consumerId +
                ", pullSize=" + pullSize +
                ", pullTimeout=" + pullTimeout +
                ", consumeTimeout=" + consumeTimeout +
                '}';
    }
}
