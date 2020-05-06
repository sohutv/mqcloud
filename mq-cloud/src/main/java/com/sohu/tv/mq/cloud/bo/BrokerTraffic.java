package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * broker流量
 * 
 * @author yongfeigao
 * @date 2018年9月28日
 */
public class BrokerTraffic {

    // 创建日期
    private Date createDate;
    // 创建时间，格式：HHMM
    private String createTime;
    // 生产次数
    private long putCount;
    // 生成大小
    private long putSize;
    // 拉取次数
    private long getCount;
    // 拉取大小
    private long getSize;
    // broker ip
    private String ip;
    // 集群id
    private int clusterId;
    // 以下为冗余字段
    // 生产次数
    private long avgPutCount;
    // 生成大小
    private long avgPutSize;
    // 拉取次数
    private long avgGetCount;
    // 拉取大小
    private long avgGetSize;

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public long getPutCount() {
        return putCount;
    }

    public void setPutCount(long putCount) {
        this.putCount = putCount;
    }

    public long getPutSize() {
        return putSize;
    }

    public void setPutSize(long putSize) {
        this.putSize = putSize;
    }

    public long getGetCount() {
        return getCount;
    }

    public void setGetCount(long getCount) {
        this.getCount = getCount;
    }

    public long getGetSize() {
        return getSize;
    }

    public void setGetSize(long getSize) {
        this.getSize = getSize;
    }

    public void add(BrokerTraffic other) {
        setGetCount(getGetCount() + other.getGetCount());
        setGetSize(getGetSize() + other.getGetSize());
        setPutCount(getPutCount() + other.getPutCount());
        setPutSize(getPutSize() + other.getPutSize());
    }

    public String getGetSizeFormat() {
        return sizeFormat(getSize);
    }

    public String getPutSizeFormat() {
        return sizeFormat(putSize);
    }

    public String getGetCountFormat() {
        return countFormat(getCount);
    }

    public String getPutCountFormat() {
        return countFormat(putCount);
    }
    
    public String getAvgGetSizeFormat() {
        return sizeFormat(avgGetSize);
    }

    public String getAvgPutSizeFormat() {
        return sizeFormat(avgPutSize);
    }

    public String getAvgGetCountFormat() {
        return countFormat(avgGetCount);
    }

    public String getAvgPutCountFormat() {
        return countFormat(avgPutCount);
    }

    private String countFormat(long value) {
        if (value >= 100000000) {
            return format(value / 100000000F) + "亿";
        }
        if (value >= 10000) {
            return format(value / 10000F) + "万";
        }
        return format(value);
    }

    private String sizeFormat(long value) {
        if (value >= 1073741824) {
            return format(value / 1073741824F) + "g";
        }
        if (value >= 1048576) {
            return format(value / 1048576F) + "m";
        }
        if (value >= 1024) {
            return format(value / 1024F) + "k";
        }
        return format(value) + "b";
    }

    private String format(float value) {
        long v = (long) (value * 10);
        if (v % 10 == 0) {
            return String.valueOf(v / 10);
        }
        return String.valueOf(v / 10.0);
    }

    public long getAvgPutCount() {
        return avgPutCount;
    }

    public void setAvgPutCount(long avgPutCount) {
        this.avgPutCount = avgPutCount;
    }

    public long getAvgPutSize() {
        return avgPutSize;
    }

    public void setAvgPutSize(long avgPutSize) {
        this.avgPutSize = avgPutSize;
    }

    public long getAvgGetCount() {
        return avgGetCount;
    }

    public void setAvgGetCount(long avgGetCount) {
        this.avgGetCount = avgGetCount;
    }

    public long getAvgGetSize() {
        return avgGetSize;
    }

    public void setAvgGetSize(long avgGetSize) {
        this.avgGetSize = avgGetSize;
    }

    @Override
    public String toString() {
        return "BrokerTraffic [createDate=" + createDate + ", createTime=" + createTime + ", putCount=" + putCount
                + ", putSize=" + putSize + ", getCount=" + getCount + ", getSize=" + getSize + ", ip=" + ip
                + ", clusterId=" + clusterId + "]";
    }
}
