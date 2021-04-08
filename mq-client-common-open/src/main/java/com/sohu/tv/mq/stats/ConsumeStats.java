package com.sohu.tv.mq.stats;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.mq.common.ConsumeException;
import com.sohu.tv.mq.stats.InvokeStats.InvokeStatsResult;

/**
 * 消费统计
 * 
 * @author yongfeigao
 * @date 2020年12月21日
 */
public class ConsumeStats {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String consumer;

    private InvokeStats invokeStats;

    private ScheduledExecutorService sampleExecutorService;

    private volatile InvokeStatsResult invokeStatsResult;

    public ConsumeStats(String consumer) {
        this(consumer, new InvokeStats());
    }

    public ConsumeStats(String consumer, InvokeStats invokeStats) {
        this.consumer = consumer;
        this.invokeStats = invokeStats;
        initTask();
    }

    /**
     * 初始化任务
     */
    private void initTask() {
        // 数据采样线程
        sampleExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "ConsumeStats-" + consumer);
            }
        });
        sampleExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    invokeStatsResult = invokeStats.sample();
                } catch (Throwable ignored) {
                    logger.warn("sample err:{}", ignored.getMessage());
                }
            }
        }, 60000, 60000, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录耗时
     * 
     * @param timeInMillis
     */
    public void increment(long timeInMillis) {
        invokeStats.increment(timeInMillis);
    }

    /**
     * 记录异常
     * 
     * @param timeInMillis
     */
    public void incrementException() {
        invokeStats.record(new ConsumeException());
    }

    public void shutdown() {
        sampleExecutorService.shutdown();
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public InvokeStatsResult getInvokeStatsResult() {
        return invokeStatsResult;
    }
}
