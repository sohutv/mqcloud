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
    // 1天前的写入量大小
    private long size1d;
    // 2天前的写入量大小
    private long size2d;
    // 3天前的写入量大小
    private long size3d;
    // 5天前的写入量大小
    private long size5d;
    // 7天前的写入量大小
    private long size7d;

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

    public long getSize1d() {
        return size1d;
    }

    public void setSize1d(long size1d) {
        this.size1d = size1d;
    }

    public long getSize2d() {
        return size2d;
    }

    public void setSize2d(long size2d) {
        this.size2d = size2d;
    }

    public long getSize3d() {
        return size3d;
    }

    public void setSize3d(long size3d) {
        this.size3d = size3d;
    }

    public long getSize5d() {
        return size5d;
    }

    public void setSize5d(long size5d) {
        this.size5d = size5d;
    }

    public long getSize7d() {
        return size7d;
    }

    public void setSize7d(long size7d) {
        this.size7d = size7d;
    }

    public void addSize1d(long count) {
        this.size1d += count;
    }

    public void addSize2d(long count) {
        this.size2d += count;
    }

    public void addSize3d(long count) {
        this.size3d += count;
    }

    public void addSize5d(long count) {
        this.size5d += count;
    }

    public void addSize7d(long count) {
        this.size7d += count;
    }

    @Override
    public String toString() {
        return "BrokerTraffic [createDate=" + createDate + ", createTime=" + createTime + ", putCount=" + putCount
                + ", putSize=" + putSize + ", getCount=" + getCount + ", getSize=" + getSize + ", ip=" + ip
                + ", clusterId=" + clusterId + "]";
    }
}
