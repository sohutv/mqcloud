package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import com.sohu.tv.mq.cloud.util.WebUtil;

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
        return WebUtil.sizeFormat(getSize);
    }

    public String getPutSizeFormat() {
        return WebUtil.sizeFormat(putSize);
    }

    public String getGetCountFormat() {
        return WebUtil.countFormat(getCount);
    }

    public String getPutCountFormat() {
        return WebUtil.countFormat(putCount);
    }
    
    public String getAvgGetSizeFormat() {
        return WebUtil.sizeFormat(avgGetSize);
    }

    public String getAvgPutSizeFormat() {
        return WebUtil.sizeFormat(avgPutSize);
    }

    public String getAvgGetCountFormat() {
        return WebUtil.countFormat(avgGetCount);
    }

    public String getAvgPutCountFormat() {
        return WebUtil.countFormat(avgPutCount);
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
