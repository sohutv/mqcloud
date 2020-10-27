package com.sohu.tv.mq.rocketmq.limiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 可以开闭的限速器
 * 
 * @author yongfeigao
 * @date 2020年7月31日
 */
public class SwitchableRateLimiter implements RateLimiter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    // 是否启用
    private volatile boolean enabled;
    // 限速器
    private RateLimiter rateLimiter;
    // 限速器名
    private String name;

    @Override
    public void limit() throws InterruptedException {
        if (enabled) {
            rateLimiter.limit();
        }
    }

    @Override
    public void setRate(int rateInSecs) {
        logger.info("{} rate changed: {}->{}", name, getRate(), rateInSecs);
        rateLimiter.setRate(rateInSecs);
    }

    @Override
    public void shutdown() {
        rateLimiter.shutdown();
    }

    public void setEnabled(boolean enabled) {
        logger.info("{} enabled changed: {}->{}", name, this.enabled, enabled);
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public void setRateLimiter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public int getRate() {
        return rateLimiter.getRate();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
