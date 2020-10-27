package com.sohu.tv.mq.rocketmq.limiter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 漏桶限速器
 * 当前请求线程看做水滴
 * 
 * @author yongfeigao
 * @date 2020年6月11日
 */
public class LeakyBucketRateLimiter implements RateLimiter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    // 漏桶名
    private String name;
    // 滴速
    private volatile int dripSpeed;
    // 滴速时间单位
    private volatile TimeUnit dripSpeedTimeUnit;
    // 漏桶
    private BlockingQueue<Thread> bucket;
    // 滴一滴水需要的纳秒
    private long dripSpeedNanos;

    private ExecutorService dripExecutorService;

    /**
     * LeakyBucketRateLimiter
     * 
     * @param name 漏桶名
     * @param bucketCapacity 桶容量
     * @param dripSpeed 滴速
     * @param dripSpeedTimeUnit 滴速单位
     */
    public LeakyBucketRateLimiter(final String name, int bucketCapacity, int dripSpeed, TimeUnit dripSpeedTimeUnit) {
        this.name = name;
        this.dripSpeed = dripSpeed;
        this.dripSpeedTimeUnit = dripSpeedTimeUnit;
        this.bucket = new ArrayBlockingQueue<>(bucketCapacity);
        dripExecutorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "LeakyBucket-" + name);
            }
        });
        caclulateDripSpeedNanos();
        leak();
    }

    /**
     * 滴水入桶，若水桶满则等待
     * 
     * @throws InterruptedException
     */
    public void drip() throws InterruptedException {
        Thread thread = Thread.currentThread();
        synchronized (thread) {
            bucket.put(thread);
            thread.wait();
        }
    }

    /**
     * 漏水，匀速
     */
    public void leak() {
        dripExecutorService.execute(new Runnable() {
            public void run() {
                while (!dripExecutorService.isShutdown()) {
                    Thread thread = null;
                    try {
                        thread = bucket.take();
                        if (thread != null) {
                            synchronized (thread) {
                                thread.notifyAll();
                            }
                        }
                        LockSupport.parkNanos(dripSpeedNanos);
                    } catch (Exception e) {
                        logger.error("leak error, thread:{}", thread, e);
                    }
                }
            }
        });
    }

    public void shutdown() {
        dripExecutorService.shutdown();
    }

    /**
     * 计算每滴需要多少纳秒
     */
    public synchronized void caclulateDripSpeedNanos() {
        this.dripSpeedNanos = dripSpeedTimeUnit.toNanos(1) / dripSpeed;
    }

    public String getName() {
        return name;
    }

    public int getDripSpeed() {
        return dripSpeed;
    }

    public void setDripSpeed(int dripSpeed) {
        this.dripSpeed = dripSpeed;
    }

    public TimeUnit getDripSpeedTimeUnit() {
        return dripSpeedTimeUnit;
    }

    public void setDripSpeedTimeUnit(TimeUnit dripSpeedTimeUnit) {
        this.dripSpeedTimeUnit = dripSpeedTimeUnit;
    }

    /**
     * 以秒为单位重置滴速
     * 
     * @param dripSpeedInSecs
     */
    public void resetDripSpeedInSecs(int dripSpeedInSecs) {
        this.dripSpeed = dripSpeedInSecs;
        this.dripSpeedTimeUnit = TimeUnit.SECONDS;
        caclulateDripSpeedNanos();
    }

    /**
     * 获取水量
     * 
     * @return
     */
    public int getWater() {
        return bucket.size();
    }

    @Override
    public void limit() throws InterruptedException {
        drip();
    }

    @Override
    public void setRate(int rateInSecs) {
        resetDripSpeedInSecs(rateInSecs);
    }

    @Override
    public int getRate() {
        return getDripSpeed();
    }
}
