package com.sohu.tv.mq.cloud.web.controller.param;
/**
 * boker store 参数
 * 
 * @author yongfeigao
 * @date 2020年4月26日
 */
public class BrokerStoreParam {
    // 集群名
    private String cluster;
    // 客户端
    private String brokerIp;
    // broker id
    private long brokerId;
    // 99%调用耗时在percent99以下
    private int percent99;
    // 90%调用耗时在percent90以下
    private int percent90;
    // 平均耗时
    private double avg;
    // 最大耗时
    private long max;
    // 总调用次数
    private long counts;
    // 统计时间 需要*60000，才是毫秒
    private int statsTime;

    public String getBrokerIp() {
        return brokerIp;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    public long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(long brokerId) {
        this.brokerId = brokerId;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

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

    public int getStatsTime() {
        return statsTime;
    }

    public void setStatsTime(int statsTime) {
        this.statsTime = statsTime;
    }

    public long getCounts() {
        return counts;
    }

    public void setCounts(long counts) {
        this.counts = counts;
    }
}
