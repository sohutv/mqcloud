package com.sohu.tv.mq.cloud.web.vo;

/**
 * http消费者配置
 *
 * @author yongfeigao
 * @date 2024年7月15日
 */
public class AuditHttpConsumerConfigVO {
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
    private String topic;
    private String consumer;

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

    public Long getConsumeTimeoutInSeconds() {
        if (consumeTimeout == null) {
            return null;
        }
        return consumeTimeout / 1000;
    }

    public Long getPullTimeoutInSeconds() {
        if (pullTimeout == null) {
            return null;
        }
        return pullTimeout / 1000;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }
}
