package com.sohu.tv.mq.cloud.bo;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
    // 重试消息跳过的key
    private String retryMessageSkipKey;

    // 消费暂停
    private Boolean pause;

    // 消费限速
    private Boolean enableRateLimit;
    private Double permitsPerSecond;

    // 暂停具体某个客户端 clientId->unregister
    private Map<String, Boolean> pauseConfig;

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

    public String getRetryMessageSkipKey() {
        return retryMessageSkipKey;
    }

    public void setRetryMessageSkipKey(String retryMessageSkipKey) {
        this.retryMessageSkipKey = retryMessageSkipKey;
    }

    public Map<String, Boolean> getPauseConfig() {
        return pauseConfig;
    }

    public void addPauseConfig(String clientId, boolean unregister) {
        if (StringUtils.isBlank(clientId)) {
            return;
        }
        if (pauseConfig == null) {
            pauseConfig = new HashMap<>();
        }
        pauseConfig.put(clientId, unregister);
    }

    public boolean containsPauseConfig(String clientId) {
        if (pauseConfig == null) {
            return false;
        }
        return pauseConfig.containsKey(clientId);
    }

    public Entry<String, Boolean> findPauseConfigFirstEntry() {
        if (pauseConfig == null) {
            return null;
        }
        return pauseConfig.entrySet().iterator().next();
    }

    @Override
    public String toString() {
        return "ConsumerConfig{" +
                "consumer='" + consumer + '\'' +
                ", retryMessageResetTo=" + retryMessageResetTo +
                ", retryMessageSkipKey='" + retryMessageSkipKey + '\'' +
                ", pause=" + pause +
                ", enableRateLimit=" + enableRateLimit +
                ", permitsPerSecond=" + permitsPerSecond +
                ", pauseConfig=" + pauseConfig +
                '}';
    }
}
