package com.sohu.tv.mq.cloud.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工具类
 *
 * @author yongfeigao
 * @date 2025年04月10日
 */
public class ThreadPoolUtil {

    /**
     * 创建一个固定大小、无队列的线程池。
     * 当线程池满时，提交线程会被阻塞，直到有线程空闲为止。
     * 不会抛出 RejectedExecutionException，也不会排队任务。
     */
    public static ThreadPoolExecutor createBlockingFixedThreadPool(String name, int poolSize) {
        return new ThreadPoolExecutor(
                poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat(name + "-%d").setDaemon(true).build(),
                (r, executor) -> {
                    try {
                        executor.getQueue().put(r);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RejectedExecutionException(e);
                    }
                });
    }
}
