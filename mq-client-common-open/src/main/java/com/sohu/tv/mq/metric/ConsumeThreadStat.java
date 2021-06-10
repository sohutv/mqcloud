package com.sohu.tv.mq.metric;
/**
 * 消费线程统计
 * 
 * @author yongfeigao
 * @date 2021年4月14日
 */

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 消费线程统计
 * 
 * @author yongfeigao
 * @date 2021年4月14日
 */
public class ConsumeThreadStat {

    // 线程消息统计
    private ConcurrentMap<Thread, MessageMetric> threadMessageMetricMap = new ConcurrentHashMap<>();

    /**
     * 设置消息统计
     * 
     * @param messageMetric
     */
    public void set(MessageMetric messageMetric) {
        threadMessageMetricMap.put(Thread.currentThread(), messageMetric);
    }

    /**
     * 移除
     */
    public void remove() {
        threadMessageMetricMap.remove(Thread.currentThread());
    }

    /**
     * 获取所有统计
     * 
     * @return
     */
    public List<StackTraceMetric> getAll() {
        List<StackTraceMetric> list = new LinkedList<>();
        threadMessageMetricMap.forEach((thread, messageMetric) -> {
            StackTraceMetric threadMetric = new StackTraceMetric(messageMetric.getStartTime(), messageMetric.getMsgIdList());
            threadMetric.initThreadMetric(thread);
            list.add(threadMetric);
        });
        return list;
    }
}
