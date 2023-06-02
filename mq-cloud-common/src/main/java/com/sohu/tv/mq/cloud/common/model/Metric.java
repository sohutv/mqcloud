package com.sohu.tv.mq.cloud.common.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * copy from rocketmq5
 *
 * @Auther: yongfeigao
 * @Date: 2023/5/31
 */
public class Metric {
    private AtomicLong count;
    private long timeStamp;

    public Metric() {
        count = new AtomicLong(0);
        timeStamp = System.currentTimeMillis();
    }

    public AtomicLong getCount() {
        return count;
    }

    public void setCount(AtomicLong count) {
        this.count = count;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", count.get(), timeStamp);
    }
}
