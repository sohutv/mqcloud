/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sohu.tv.mq.rocketmq.limiter;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.locks.LockSupport;

/**
 * A rate limiter. Conceptually, a rate limiter distributes permits at a
 * configurable rate. Each {@link #acquire()} blocks if necessary until a permit
 * is available, and then takes it. Once acquired, permits need not be released.
 * <p>
 * Rate limiters are often used to restrict the rate at which some physical or
 * logical resource is accessed. This is in contrast to
 * {@link java.util.concurrent.Semaphore} which restricts the number of
 * concurrent accesses instead of the rate (note though that concurrency and
 * rate are closely related, e.g. see
 * <a href="http://en.wikipedia.org/wiki/Little's_law">Little's Law</a>).
 * <p>
 * A {@code RateLimiter} is defined primarily by the rate at which permits are
 * issued. Absent additional configuration, permits will be distributed at a
 * fixed rate, defined in terms of permits per second. Permits will be
 * distributed smoothly, with the delay between individual permits being
 * adjusted to ensure that the configured rate is maintained.
 * <p>
 * It is possible to configure a {@code RateLimiter} to have a warmup period
 * during which time the permits issued each second steadily increases until it
 * hits the stable rate.
 * <p>
 * As an example, imagine that we have a list of tasks to execute, but we don't
 * want to submit more than 2 per second:
 * 
 * <pre>
 *   {@code
 *  final RateLimiter rateLimiter = RateLimiter.create(2.0); // rate is "2 permits per second"
 *  void submitTasks(List<Runnable> tasks, Executor executor) {
 *    for (Runnable task : tasks) {
 *      rateLimiter.acquire(); // may wait
 *      executor.execute(task);
 *    }
 *  }
 *}
 * </pre>
 * <p>
 * As another example, imagine that we produce a stream of data, and we want to
 * cap it at 5kb per second. This could be accomplished by requiring a permit
 * per byte, and specifying a rate of 5000 permits per second:
 * 
 * <pre>
 *   {@code
 *  final RateLimiter rateLimiter = RateLimiter.create(5000.0); // rate = 5000 permits per second
 *  void submitPacket(byte[] packet) {
 *    rateLimiter.acquire(packet.length);
 *    networkService.send(packet);
 *  }
 *}
 * </pre>
 * <p>
 * It is important to note that the number of permits requested <i>never</i>
 * affect the throttling of the request itself (an invocation to
 * {@code acquire(1)} and an invocation to {@code acquire(1000)} will result in
 * exactly the same throttling, if any), but it affects the throttling of the
 * <i>next</i> request. I.e., if an expensive task arrives at an idle
 * RateLimiter, it will be granted immediately, but it is the <i>next</i>
 * request that will experience extra throttling, thus paying for the cost of
 * the expensive task.
 * <p>
 * Note: {@code RateLimiter} does not provide fairness guarantees.
 *
 * @author Dimitris Andreou
 * @since 13.0
 */
public class TokenBucketRateLimiter implements RateLimiter {
    /**
     * The currently stored permits.
     */
    double storedPermits;

    /**
     * The maximum number of stored permits.
     */
    double maxPermits;

    /**
     * The interval between two unit requests, at our stable rate. E.g., a
     * stable rate of 5 permits per second has a stable interval of 200ms.
     */
    double stableIntervalMicros;

    /**
     * The time when the next request (no matter its size) will be granted.
     * After granting a request, this is pushed further in the future. Large
     * requests push this further than small requests.
     */
    private long nextFreeTicketMicros = 0L;

    private long startTick = System.nanoTime();

    public TokenBucketRateLimiter(double permitsPerSecond) {
        setRate(permitsPerSecond);
    }

    public double acquire() throws InterruptedException {
        return acquire(1);
    }

    public double acquire(int permits) {
        long microsToWait = reserve(permits);
        if (microsToWait > 0) {
            LockSupport.parkNanos(MICROSECONDS.toNanos(microsToWait));
        }
        return 1.0 * microsToWait / SECONDS.toMicros(1L);
    }

    public final void setRate(double permitsPerSecond) {
        synchronized (this) {
            doSetRate(permitsPerSecond, readMicros());
        }
    }

    final void doSetRate(double permitsPerSecond, long nowMicros) {
        resync(nowMicros);
        this.stableIntervalMicros = SECONDS.toMicros(1L) / permitsPerSecond;
        double oldMaxPermits = this.maxPermits;
        maxPermits = permitsPerSecond;
        if (oldMaxPermits == Double.POSITIVE_INFINITY) {
            storedPermits = maxPermits;
        } else {
            storedPermits = (oldMaxPermits == 0.0) ? 0.0 : storedPermits * maxPermits / oldMaxPermits;
        }
    }

    final long reserve(int permits) {
        synchronized (this) {
            return reserveAndGetWaitLength(permits, readMicros());
        }
    }

    final long reserveAndGetWaitLength(int permits, long nowMicros) {
        long momentAvailable = reserveEarliestAvailable(permits, nowMicros);
        return max(momentAvailable - nowMicros, 0);
    }

    final long reserveEarliestAvailable(int requiredPermits, long nowMicros) {
        resync(nowMicros);
        long returnValue = nextFreeTicketMicros;
        double storedPermitsToSpend = min(requiredPermits, this.storedPermits);
        double freshPermits = requiredPermits - storedPermitsToSpend;
        long waitMicros = (long) (freshPermits * stableIntervalMicros);
        this.nextFreeTicketMicros += waitMicros;
        this.storedPermits -= storedPermitsToSpend;
        return returnValue;
    }

    private void resync(long nowMicros) {
        if (nowMicros > nextFreeTicketMicros) {
            storedPermits = min(maxPermits, storedPermits + (nowMicros - nextFreeTicketMicros) / stableIntervalMicros);
            nextFreeTicketMicros = nowMicros;
        }
    }

    long readMicros() {
        return MICROSECONDS.convert(System.nanoTime() - startTick, NANOSECONDS);
    }

    @Override
    public void limit() throws InterruptedException {
        acquire();
    }

    @Override
    public void setRate(int rateInSecs) {
        setRate((double) rateInSecs);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public synchronized int getRate() {
        return (int) (SECONDS.toMicros(1L) / stableIntervalMicros);
    }
}