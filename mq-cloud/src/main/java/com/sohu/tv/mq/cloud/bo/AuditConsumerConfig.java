package com.sohu.tv.mq.cloud.bo;

/**
 * 消费者配置审核
 * 
 * @author yongfeigao
 * @date 2020年6月4日
 */
public class AuditConsumerConfig {
    // 审核id
    private long aid;
    // 消费者id
    private long consumerId;
    // 重试消息跳过
    private Long retryMessageResetTo;

    // 消费暂停
    private Boolean pause;
    private String pauseClientId;
    // 消费限速
    private Boolean enableRateLimit;
    private Double permitsPerSecond;
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

    public Long getRetryMessageResetTo() {
        return retryMessageResetTo;
    }

    public void setRetryMessageResetTo(Long retryMessageResetTo) {
        this.retryMessageResetTo = retryMessageResetTo;
    }

    public Boolean getPause() {
        return pause;
    }

    public void setPause(Boolean pause) {
        this.pause = pause;
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

    public String getPauseClientId() {
        return pauseClientId;
    }

    public void setPauseClientId(String pauseClientId) {
        this.pauseClientId = pauseClientId;
    }

    public Boolean getUnregister() {
        return unregister;
    }

    public void setUnregister(Boolean unregister) {
        this.unregister = unregister;
    }

    @Override
    public String toString() {
        return "AuditConsumerConfig [aid=" + aid + ", consumerId=" + consumerId + ", retryMessageResetTo="
                + retryMessageResetTo + ", pause=" + pause + ", pauseClientId=" + pauseClientId + ", enableRateLimit="
                + enableRateLimit + ", permitsPerSecond=" + permitsPerSecond + "]";
    }
}
