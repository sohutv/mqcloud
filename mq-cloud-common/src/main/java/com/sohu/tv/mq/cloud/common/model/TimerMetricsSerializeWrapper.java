package com.sohu.tv.mq.cloud.common.model;

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * copy from rocketmq5
 *
 * @Auther: yongfeigao
 * @Date: 2023/5/31
 */
public class TimerMetricsSerializeWrapper extends RemotingSerializable {
    private ConcurrentMap<String, Metric> timingCount = new ConcurrentHashMap<String, Metric>();

    public ConcurrentMap<String, Metric> getTimingCount() {
        return timingCount;
    }

    public void setTimingCount(ConcurrentMap<String, Metric> timingCount) {
        this.timingCount = timingCount;
    }
}
