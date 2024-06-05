package com.sohu.tv.mq.cloud.bo;

/**
 * topic流量
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月26日
 */
public class TopicTraffic extends Traffic {

    private long tid;

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

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
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
        return "TopicTraffic{" +
                "tid=" + tid +
                ", size1d=" + size1d +
                ", size2d=" + size2d +
                ", size3d=" + size3d +
                ", size5d=" + size5d +
                ", size7d=" + size7d +
                "} " + super.toString();
    }
}
