package com.sohu.tv.mq.cloud.common.model;

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 百分位耗时统计
 * 
 * @author yongfeigao
 * @date 2020年4月28日
 */
public class PercentileStat extends RemotingSerializable {
    // 99%调用耗时在percent99以下
    private int percent99;
    // 90%调用耗时在percent90以下
    private int percent90;
    // 平均耗时
    private double avg;
    // 最大耗时
    private int max;
    // 总调用次数
    private long count;
    // 统计时间 需要*60000，才是毫秒
    private int statTime;

    public int getPercent99() {
        return percent99;
    }

    public void setPercent99(int percent99) {
        this.percent99 = percent99;
    }

    public int getPercent90() {
        return percent90;
    }

    public void setPercent90(int percent90) {
        this.percent90 = percent90;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getStatTime() {
        return statTime;
    }

    public void setStatTime(int statTime) {
        this.statTime = statTime;
    }

    @Override
    public String toString() {
        return "percent99=" + percent99 + ", percent90=" + percent90 + ", avg=" + avg + ", max=" + max + ", count="
                + count + ", statTime=" + statTime;
    }
}
