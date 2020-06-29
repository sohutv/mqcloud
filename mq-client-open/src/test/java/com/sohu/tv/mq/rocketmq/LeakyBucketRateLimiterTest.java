package com.sohu.tv.mq.rocketmq;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class LeakyBucketRateLimiterTest {

    private LeakyBucketRateLimiter leakyBucketRateLimiter = new LeakyBucketRateLimiter("test", 10, 1, TimeUnit.SECONDS);

    @Test
    public void testSingleThread() throws InterruptedException {
        drip();
        drip();
        drip();
    }

    @Test
    public void testMultiThread() throws InterruptedException {
        int threadNum = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; ++i) {
            new Thread() {
                public void run() {
                    try {
                        drip();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    countDownLatch.countDown();
                }
            }.start();
        }
        countDownLatch.await();
        System.out.println("over");
    }

    @Test
    public void testMultiThreadChangeDripSpeed() throws InterruptedException {
        int threadNum = 30;
        final Semaphore semaphore = new Semaphore(threadNum);
        for (int i = 0; i < threadNum; ++i) {
            new DripTask(semaphore).start();
        }
        while(semaphore.availablePermits() != threadNum) {
            Thread.sleep(1000);
        }
    }

    public void drip() throws InterruptedException {
        long start = System.currentTimeMillis();
        leakyBucketRateLimiter.drip();
        System.out.println(Thread.currentThread() + " drip use " + (System.currentTimeMillis() - start));
    }

    public class DripTask extends Thread {
        private Semaphore semaphore;

        public DripTask(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        public void run() {
            try {
                semaphore.acquire();
                drip();
                if (semaphore.availablePermits() == 10) {
                    leakyBucketRateLimiter.resetDripSpeedInSecs(2);
                } else if (semaphore.availablePermits() == 20) {
                    leakyBucketRateLimiter.resetDripSpeedInSecs(1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }
    }
}
