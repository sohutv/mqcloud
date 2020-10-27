package com.sohu.tv.mq.cloud.bo;

/**
 * @author yongweizhao
 * @create 2020/8/3 17:46
 */
public class TopicTrafficStat {

    // topic id
    private long tid;

    // 前days天内，每天流量最大值的平均值
    private long avgMax;

    // 前days天内，去除异常点后的流量的最大值
    private long maxMax;

    // 前days天
    private int days;

    public TopicTrafficStat() {}

    public TopicTrafficStat(long tid, long avgMax, long maxMax, int days) {
        this.tid = tid;
        this.avgMax = avgMax;
        this.maxMax = maxMax;
        this.days = days;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public long getAvgMax() {
        return avgMax;
    }

    public void setAvgMax(long avgMax) {
        this.avgMax = avgMax;
    }

    public long getMaxMax() {
        return maxMax;
    }

    public void setMaxMax(long maxMax) {
        this.maxMax = maxMax;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return "TopicTrafficStat{" +
                "tid=" + tid +
                ", avgMax=" + avgMax +
                ", maxMax=" + maxMax +
                ", days=" + days +
                '}';
    }
}
