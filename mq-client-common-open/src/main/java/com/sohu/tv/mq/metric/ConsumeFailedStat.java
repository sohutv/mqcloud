package com.sohu.tv.mq.metric;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消费失败统计
 * 
 * @author yongfeigao
 * @date 2021年4月29日
 */
public class ConsumeFailedStat {
    // 计数数组的下标
    private volatile AtomicLong indexer = new AtomicLong();

    private MessageExceptionMetric[] messageExceptionMetricArray;

    public ConsumeFailedStat(int size) {
        messageExceptionMetricArray = new MessageExceptionMetric[size];
    }

    public void set(MessageExceptionMetric messageExceptionMetric) {
        int index = (int) (indexer.getAndIncrement() % messageExceptionMetricArray.length);
        // 溢出重置
        if (index < 0) {
            indexer.set(0);
            index = 0;
        }
        messageExceptionMetricArray[index] = messageExceptionMetric;
    }

    /**
     * 获取所有统计
     * 
     * @return
     */
    public List<StackTraceMetric> getAll() {
        List<StackTraceMetric> list = new LinkedList<>();
        for (MessageExceptionMetric metric : messageExceptionMetricArray) {
            if (metric == null) {
                continue;
            }
            StackTraceMetric threadMetric = new StackTraceMetric(metric.getStartTime(), metric.getMsgIdList());
            threadMetric.setId(metric.getThreadId());
            threadMetric.setName(metric.getThreadName());
            threadMetric.setStackTraceArray(metric.getException().getStackTrace());
            threadMetric.setErrorClass(metric.getException().getClass().toString());
            threadMetric.setMessage(metric.getException().getMessage());
            list.add(threadMetric);
        }
        return list;
    }
}
