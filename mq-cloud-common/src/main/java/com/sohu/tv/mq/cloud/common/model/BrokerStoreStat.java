package com.sohu.tv.mq.cloud.common.model;

/**
 * broker存储统计
 * 
 * @author yongfeigao
 * @date 2020年4月26日
 */
public class BrokerStoreStat {
    // clusterId
    private int clusterId = -1;
    // broker ip
    private String brokerIp;
    // 百分之90
    private int percent90;
    // 百分之99
    private int percent99;
    // 平均耗时
    private double avg;
    // 调用次数
    private int max;
    // 调用次数
    private long count;
    // 统计时间
    private int statTime;
    // 创建日期
    private int createDate;
    // 创建时间
    private String createTime;
    
    private String clusterName;
    
    private String brokerStoreLink;

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public int getPercent90() {
        return percent90;
    }

    public void setPercent90(int percent90) {
        this.percent90 = percent90;
    }

    public int getPercent99() {
        return percent99;
    }

    public void setPercent99(int percent99) {
        this.percent99 = percent99;
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

    public int getCreateDate() {
        return createDate;
    }

    public void setCreateDate(int createDate) {
        this.createDate = createDate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getBrokerIp() {
        return brokerIp;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getBrokerStoreLink() {
        return brokerStoreLink;
    }

    public void setBrokerStoreLink(String brokerStoreLink) {
        this.brokerStoreLink = brokerStoreLink;
    }
}
