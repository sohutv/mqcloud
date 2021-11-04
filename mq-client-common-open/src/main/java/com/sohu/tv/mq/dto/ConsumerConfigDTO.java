package com.sohu.tv.mq.dto;

/**
 * 消费者动态配置
 * 
 * @author yongfeigao
 * @date 2020年6月3日
 */
public class ConsumerConfigDTO {
    // 重试消息跳过
    private Long retryMessageResetTo;

    // 消费暂停
    private Boolean pause;
    private String pauseClientId;

    // 消费限速
    private Boolean enableRateLimit;
    private Double permitsPerSecond;
    
    // 重试消息跳过的key
    private String retryMessageSkipKey;

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

    public String getRetryMessageSkipKey() {
        return retryMessageSkipKey;
    }

    public void setRetryMessageSkipKey(String retryMessageSkipKey) {
        this.retryMessageSkipKey = retryMessageSkipKey;
    }

    @Override
    public String toString() {
        return "ConsumerConfigDTO [retryMessageResetTo=" + retryMessageResetTo + ", pause=" + pause + ", pauseClientId="
                + pauseClientId + ", enableRateLimit=" + enableRateLimit + ", permitsPerSecond=" + permitsPerSecond
                + ", retryMessageSkipKey=" + retryMessageSkipKey + "]";
    }
}
