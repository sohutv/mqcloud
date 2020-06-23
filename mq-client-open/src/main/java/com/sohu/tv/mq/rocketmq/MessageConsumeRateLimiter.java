package com.sohu.tv.mq.rocketmq;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.mq.util.Constant;

/**
 * 消息消费速率限制
 * 
 * @author yongfeigao
 * @date 2020年6月3日
 */
public class MessageConsumeRateLimiter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // 是否启用
    private volatile boolean enableRateLimit;
    // 限速器
    private LeakyBucketRateLimiter rateLimiter;

    public MessageConsumeRateLimiter(String name, int bucketCapacity) {
        rateLimiter = new LeakyBucketRateLimiter(name, bucketCapacity, Constant.LIMIT_CONSUME_TPS, TimeUnit.SECONDS);
    }

    /**
     * 获取许可
     * 
     * @throws InterruptedException
     */
    public void acquire() throws InterruptedException {
        if (enableRateLimit) {
            rateLimiter.drip();
        }
    }

    /**
     * 设置速率
     * 
     * @param permitsPerSecond
     */
    public void setRate(int permitsPerSecond) {
        rateLimiter.resetDripSpeedInSecs(permitsPerSecond);
    }

    public boolean isEnableRateLimit() {
        return enableRateLimit;
    }
    
    public int getRate() {
        return rateLimiter.getDripSpeed();
    }
    
    public void shutdown() {
        rateLimiter.shutdown();
    }

    /**
     * 设置是否启用限速器
     * 
     * @param enableRateLimit
     */
    public void setEnableRateLimit(boolean enableRateLimit) {
        logger.info("enableRateLimit changed: {}->{}", this.enableRateLimit, enableRateLimit);
        this.enableRateLimit = enableRateLimit;
    }
}
