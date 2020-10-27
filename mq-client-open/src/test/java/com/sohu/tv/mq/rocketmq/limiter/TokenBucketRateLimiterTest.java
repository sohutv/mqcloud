package com.sohu.tv.mq.rocketmq.limiter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class TokenBucketRateLimiterTest {

    private TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1);

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

    public void drip() throws InterruptedException {
        long start = System.currentTimeMillis();
        rateLimiter.acquire();
        System.out.println(Thread.currentThread() + " drip use " + (System.currentTimeMillis() - start));
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

    public class DripTask extends Thread {
        private Semaphore semaphore;

        public DripTask(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        public void run() {
            try {
                semaphore.acquire();
                if (semaphore.availablePermits() == 20) {
                    rateLimiter.setRate(2);
                }
                drip();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }
    }

    @Test
    public void testPerformance() throws InterruptedException {
        final AtomicLong counter = new AtomicLong();
        rateLimiter = new TokenBucketRateLimiter(1000000);
        System.out.println(rateLimiter.getRate());
        for (int i = 0; i < 20; ++i) {
            new Thread() {
                public void run() {
                    try {
                        while (true) {
                            rateLimiter.acquire();
                            counter.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        long start = System.currentTimeMillis();
        while (true) {
            Thread.sleep(1000);
            System.out.println("qps=" + (counter.get() * 1000D / (System.currentTimeMillis() - start)));
        }
    }

}
