package com.sohu.tv.mq.cloud.web.controller.param;

import com.sohu.tv.mq.util.MQProtocol;
import org.hibernate.validator.constraints.Range;

import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;

/**
 * 消费者配置审核
 * 
 * @author yongfeigao
 * @date 2020年6月4日
 */
public class ConsumerConfigParam {
    // 消费者id
    @Range(min = 1)
    private long consumerId;
    // 消费暂停
    private Boolean pause;
    private String pauseClientId;
    // 消费限速
    private Boolean enableRateLimit;
    private Double permitsPerSecond;
    // 是否解注册
    private boolean unregister;
    private long tid;
    private String consumer;
    private int protocol;

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

    public boolean getUnregister() {
        return unregister;
    }

    public void setUnregister(boolean unregister) {
        this.unregister = unregister;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public boolean isHttpProtocol() {
        return protocol == MQProtocol.HTTP.getType();
    }

    /**
     * 获取type
     * @return
     */
    public TypeEnum getType() {
        if (getPause() != null) {
            if (getPause()) {
                return TypeEnum.PAUSE_CONSUME;
            }
            return TypeEnum.RESUME_CONSUME;
        }
        if (getEnableRateLimit() != null || getPermitsPerSecond() != null) {
            return TypeEnum.LIMIT_CONSUME;
        }
        return null;
    }

    @Override
    public String toString() {
        return "ConsumerConfigParam [consumerId=" + consumerId + ", pause=" + pause + ", pauseClientId=" + pauseClientId
                + ", enableRateLimit=" + enableRateLimit + ", permitsPerSecond=" + permitsPerSecond + "]";
    }
}
