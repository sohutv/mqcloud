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
import static java.util.concurrent.TimeUnit.*;

/**
 * 令牌桶限速器-改造自guava
 * 增加熔断机制，超阈值自动熔断1s后恢复
 * @author: yongfeigao
 * @date: 2022/3/4 10:22
 */
public class NoneBlockingRateLimiter {
    /**
     * The currently stored permits.
     */
    double storedPermits;

    /**
     * The maximum number of stored permits.
     */
    double maxPermits;

    /**
     * The interval between two unit requests, at our stable rate. E.g., a stable
     * rate of 5 permits per second has a stable interval of 200ms.
     */
    double stableIntervalMicros;

    /**
     * The time when the next request (no matter its size) will be granted. After
     * granting a request, this is pushed further in the future. Large requests push
     * this further than small requests.
     */
    private long nextFreeTicketMicros = 0L;

    private long startTick = System.nanoTime();
    // 熔断器是否打开
    private boolean circuitBreakerOpen;
    // 上次需要等待的纳秒
    private long lastNeedWaitMicrosecs = -1;
    // 上次限速时间戳
    private long lastRateLimitTimestamp = -1;
    // 熔断时间，微秒
    private long circuitBreakerOpenTimeInMicros = SECONDS.toMicros(1);

    // 是否启用
    private boolean enabled = true;

    public NoneBlockingRateLimiter(double permitsPerSecond) {
        setRate(permitsPerSecond);
    }

    /**
     * 获取令牌
     * @return true:获取到令牌, false:未获取令牌
     */
    public boolean acquire() {
        if (!enabled) {
            return true;
        }
        return acquire(1);
    }

    /**
     * 获取令牌
     * @param permits
     * @return
     */
    public boolean acquire(int permits) {
        if (!enabled) {
            return true;
        }
        synchronized (this) {
            // 获取启动以来的时间
            long nowMicros = readMicros();
            // 熔断器打开
            if (circuitBreakerOpen) {
                // 当前时间超过可以预支的时间阈值后，流量放开
                if (nowMicros - nextFreeTicketMicros >= circuitBreakerOpenTimeInMicros) {
                    circuitBreakerOpen = false;
                }
            }
            // 熔断器未打开
            if (!circuitBreakerOpen) {
                // 获取需要等待的时间
                lastNeedWaitMicrosecs = reserveAndGetWaitLength(permits, nowMicros);
                // 需要等待证明熔断器打开
                if (lastNeedWaitMicrosecs > 0) {
                    circuitBreakerOpen = true;
                    // 记录限流时间戳
                    lastRateLimitTimestamp = System.currentTimeMillis();
                }
            }
            // 熔断器未打开，则可以获取令牌
            return !circuitBreakerOpen;
        }
    }

    public long reserveAndGetWaitLength(int permits, long nowMicros) {
        long momentAvailable = reserveEarliestAvailable(permits, nowMicros);
        return max(momentAvailable - nowMicros, 0);
    }

    /**
     * 预留可用时间
     * @param requiredPermits
     * @param nowMicros
     * @return
     */
    public long reserveEarliestAvailable(int requiredPermits, long nowMicros) {
        resync(nowMicros);
        long returnValue = nextFreeTicketMicros;
        double storedPermitsToSpend = min(requiredPermits, this.storedPermits);
        double freshPermits = requiredPermits - storedPermitsToSpend;
        long waitMicros = (long) (freshPermits * stableIntervalMicros);
        this.nextFreeTicketMicros += waitMicros;
        this.storedPermits -= storedPermitsToSpend;
        return returnValue;
    }

    /**
     * 同步时间
     * @param nowMicros
     */
    public void resync(long nowMicros) {
        if (nowMicros > nextFreeTicketMicros) {
            storedPermits = min(maxPermits, storedPermits + (nowMicros - nextFreeTicketMicros) / stableIntervalMicros);
            nextFreeTicketMicros = nowMicros;
        }
    }

    /**
     * 重置qps
     * @param permitsPerSecond
     */
    public final void setRate(double permitsPerSecond) {
        synchronized (this) {
            doSetRate(permitsPerSecond, readMicros());
        }
    }

    public void doSetRate(double permitsPerSecond, long nowMicros) {
        resync(nowMicros);
        this.stableIntervalMicros = SECONDS.toMicros(1L) / permitsPerSecond;
        double oldMaxPermits = this.maxPermits;
        maxPermits = permitsPerSecond;
        if (oldMaxPermits == Double.POSITIVE_INFINITY) {
            storedPermits = maxPermits;
        } else {
            if (oldMaxPermits == 0.0) {
                if (maxPermits == 0.0) {
                    storedPermits = 0.0;
                } else {
                    // 防止初始化即被限流
                    storedPermits = maxPermits;
                }
            } else {
                storedPermits *= maxPermits / oldMaxPermits;
            }
        }
    }

    public void setCircuitBreakerOpenTimeInMicros(long circuitBreakerOpenTimeInMicros) {
        synchronized (this) {
            this.circuitBreakerOpenTimeInMicros = circuitBreakerOpenTimeInMicros;
        }
    }

    /**
     * 获取启动以来的微秒
     * @return
     */
    long readMicros() {
        return MICROSECONDS.convert(System.nanoTime() - startTick, NANOSECONDS);
    }
    
    public double getQps() {
        return maxPermits;
    }

    public long getLastNeedWaitMicrosecs() {
        return lastNeedWaitMicrosecs;
    }

    public long getLastRateLimitTimestamp() {
        return lastRateLimitTimestamp;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}