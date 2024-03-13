package com.sohu.tv.mq.dto;

import java.util.Map;

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

    // 消费限速
    private Boolean enableRateLimit;
    private Double permitsPerSecond;
    
    // 重试消息跳过的key
    private String retryMessageSkipKey;

    // 暂停具体某个客户端 clientId->unregister
    private Map<String, Boolean> pauseConfig;

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

    public String getRetryMessageSkipKey() {
        return retryMessageSkipKey;
    }

    public void setRetryMessageSkipKey(String retryMessageSkipKey) {
        this.retryMessageSkipKey = retryMessageSkipKey;
    }

    public Map<String, Boolean> getPauseConfig() {
        return pauseConfig;
    }

    public void setPauseConfig(Map<String, Boolean> pauseConfig) {
        this.pauseConfig = pauseConfig;
    }

    @Override
    public String toString() {
        return "ConsumerConfigDTO{" +
                "retryMessageResetTo=" + retryMessageResetTo +
                ", pause=" + pause +
                ", enableRateLimit=" + enableRateLimit +
                ", permitsPerSecond=" + permitsPerSecond +
                ", retryMessageSkipKey='" + retryMessageSkipKey + '\'' +
                ", pauseConfig=" + pauseConfig +
                '}';
    }
}
