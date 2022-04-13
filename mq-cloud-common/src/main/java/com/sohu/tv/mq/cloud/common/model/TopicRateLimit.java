package com.sohu.tv.mq.cloud.common.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * topic限速
 * 
 * @author yongfeigao
 * @date 2022年2月22日
 */
public class TopicRateLimit {
    // topic
    private String topic;
    // 默认限速qps
    private double limitQps;
    // 上次需要等待的时间
    private long lastNeedWaitMicrosecs;
    // 上次限速的时间戳
    private long lastRateLimitTimestamp;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public double getLimitQps() {
        return limitQps;
    }

    public void setLimitQps(double limitQps) {
        this.limitQps = limitQps;
    }

    public long getLastNeedWaitMicrosecs() {
        return lastNeedWaitMicrosecs;
    }

    public void setLastNeedWaitMicrosecs(long lastNeedWaitMicrosecs) {
        this.lastNeedWaitMicrosecs = lastNeedWaitMicrosecs;
    }

    public long getLastRateLimitTimestamp() {
        return lastRateLimitTimestamp;
    }

    public void setLastRateLimitTimestamp(long lastRateLimitTimestamp) {
        this.lastRateLimitTimestamp = lastRateLimitTimestamp;
    }

    public String getLastRateLimitTimestampFormat(){
        if (lastRateLimitTimestamp <= 0) {
            return String.valueOf(lastRateLimitTimestamp);
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(lastRateLimitTimestamp));
    }
}
