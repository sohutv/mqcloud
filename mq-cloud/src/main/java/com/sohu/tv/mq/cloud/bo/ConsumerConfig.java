package com.sohu.tv.mq.cloud.bo;

/**
 * 消费者配置
 * 
 * @author yongfeigao
 * @date 2020年6月3日
 */
public class ConsumerConfig {
    // 消费者
    private String consumer;
    // 重试消息跳过
    private Long retryMessageResetTo;

    // 消费暂停
    private Boolean pause;
    private String pauseClientId;

    // 消费限速
    private Boolean enableRateLimit;
    private Double permitsPerSecond;

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((consumer == null) ? 0 : consumer.hashCode());
        result = prime * result + ((enableRateLimit == null) ? 0 : enableRateLimit.hashCode());
        result = prime * result + ((pause == null) ? 0 : pause.hashCode());
        result = prime * result + ((pauseClientId == null) ? 0 : pauseClientId.hashCode());
        result = prime * result + ((permitsPerSecond == null) ? 0 : permitsPerSecond.hashCode());
        result = prime * result + ((retryMessageResetTo == null) ? 0 : retryMessageResetTo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConsumerConfig other = (ConsumerConfig) obj;
        if (consumer == null) {
            if (other.consumer != null)
                return false;
        } else if (!consumer.equals(other.consumer))
            return false;
        if (enableRateLimit == null) {
            if (other.enableRateLimit != null)
                return false;
        } else if (!enableRateLimit.equals(other.enableRateLimit))
            return false;
        if (pause == null) {
            if (other.pause != null)
                return false;
        } else if (!pause.equals(other.pause))
            return false;
        if (pauseClientId == null) {
            if (other.pauseClientId != null)
                return false;
        } else if (!pauseClientId.equals(other.pauseClientId))
            return false;
        if (permitsPerSecond == null) {
            if (other.permitsPerSecond != null)
                return false;
        } else if (!permitsPerSecond.equals(other.permitsPerSecond))
            return false;
        if (retryMessageResetTo == null) {
            if (other.retryMessageResetTo != null)
                return false;
        } else if (!retryMessageResetTo.equals(other.retryMessageResetTo))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConsumerConfig [consumer=" + consumer + ", retryMessageResetTo=" + retryMessageResetTo + ", pause="
                + pause + ", pauseClientId=" + pauseClientId + ", enableRateLimit=" + enableRateLimit + ", permitsPerSecond="
                + permitsPerSecond + "]";
    }
}
