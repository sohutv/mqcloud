package com.sohu.tv.mq.cloud.web.vo;

/**
 * 消费者配置审核
 * 
 * @author yongfeigao
 * @date 2020年6月4日
 */
public class AuditConsumerConfigVO {
    // 审核id
    private long aid;
    // 消费者id
    private long consumerId;

    // 消费暂停
    private Boolean pause;
    private String pauseClientId;

    // 消费限速
    private Boolean enableRateLimit;
    private Double permitsPerSecond;

    private String topic;
    private String consumer;

    // 是否解注册
    private Boolean unregister;

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

    public Boolean getPause() {
        return pause;
    }

    public void setPause(Boolean pause) {
        this.pause = pause;
    }

    public String getPauseClientId() {
        return pauseClientId;
    }

    public void setPauseClientId(String pauseClientId) {
        this.pauseClientId = pauseClientId;
    }

    public Boolean getEnableRateLimit() {
        return enableRateLimit;
    }

    public void setEnableRateLimit(Boolean enableRateLimit) {
        this.enableRateLimit = enableRateLimit;
    }

    public Double getPermitsPerSecond() {
        return permitsPerSecond;
    }

    public void setPermitsPerSecond(Double permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
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

    public Boolean getUnregister() {
        return unregister;
    }

    public void setUnregister(Boolean unregister) {
        this.unregister = unregister;
    }
}
