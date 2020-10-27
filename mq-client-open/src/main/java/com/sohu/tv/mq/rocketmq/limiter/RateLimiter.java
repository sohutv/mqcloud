package com.sohu.tv.mq.rocketmq.limiter;

/**
 * 限速器
 * 
 * @author yongfeigao
 * @date 2020年7月31日
 */
public interface RateLimiter {
    /**
     * 限速，达到速度将阻塞当前线程
     */
    void limit() throws InterruptedException;

    /**
     * 获取速率
     */
    int getRate();

    /**
     * 速率,单位秒
     * 
     * @param rateInSecs
     */
    void setRate(int rateInSecs);

    /**
     * 关闭
     */
    void shutdown();

}
