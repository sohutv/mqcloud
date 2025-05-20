package com.sohu.tv.mq.cloud.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolUtilTest {

    private ThreadPoolExecutor topicTrafficFetchThreadPool =
            ThreadPoolUtil.createBlockingFixedThreadPool("topicTrafficFetch", 2);

    @Test
    public void test() {
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            final int j = i;
            Future future = topicTrafficFetchThreadPool.submit(() -> {
                System.out.println("Thread: " + Thread.currentThread().getName() + ", Task: " + j);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }
        // 等待所有任务执行完毕
        for (Future future : futures) {
            try {
                future.get();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        System.out.println("All tasks completed.");
    }

}