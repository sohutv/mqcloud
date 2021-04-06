package com.sohu.tv.mq.metric;

import java.io.Serializable;
/**
 * mq指标
 * 
 * @author yongfeigao
 * @date 2020年12月22日
 */
public class MQMetrics implements Serializable {
    private static final long serialVersionUID = 738296259571004441L;
    // 最大耗时
    private int maxTime;
    // 总耗时
    private long totalTime;
    // 调用次数
    private int totalCount;
    // 异常次数
    private int exceptionCount;
    // 生产或消费组
    private String group;

    public int getMaxTime() {
        return maxTime;
    }

    public MQMetrics setMaxTime(int maxTime) {
        if (maxTime > this.maxTime) {
            this.maxTime = maxTime;
        }
        return this;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public MQMetrics addTotalTime(long totalTime) {
        this.totalTime += totalTime;
        return this;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public MQMetrics addTotalCount(int totalCount) {
        this.totalCount += totalCount;
        return this;
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(int exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    public MQMetrics addExceptionCount(int exceptionCount) {
        this.exceptionCount += exceptionCount;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "MQMetrics [maxTime=" + maxTime + ", totalTime=" + totalTime + ", totalCount=" + totalCount
                + ", exceptionCount=" + exceptionCount + ", group=" + group + "]";
    }
}
