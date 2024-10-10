package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.common.util.WebUtil;

/**
 * broker
 * 
 * @author yongfeigao
 * @date 2018年11月14日
 */
public class Broker extends DeployableComponent {
    // broker id
    private int brokerID;
    // broker name
    private String brokerName;
    
    // 最大偏移量 冗余字段
    private long maxOffset;

    // 是否可以写入
    private boolean writable = true;

    // 1天前的流量
    private long size1d;
    // 2天前的流量
    private long size2d;
    // 3天前的流量
    private long size3d;
    // 5天前的流量
    private long size5d;
    // 7天前的流量
    private long size7d;
    
    public int getBrokerID() {
        return brokerID;
    }

    public void setBrokerID(int brokerID) {
        this.brokerID = brokerID;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }
    
    public boolean isMaster() {
        return brokerID == 0;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
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

    public String getSize1dFormat() {
        return WebUtil.sizeFormat(size1d);
    }

    public String getSize2dFormat() {
        return WebUtil.sizeFormat(size2d);
    }

    public String getSize3dFormat() {
        return WebUtil.sizeFormat(size3d);
    }

    public String getSize5dFormat() {
        return WebUtil.sizeFormat(size5d);
    }

    public String getSize7dFormat() {
        return WebUtil.sizeFormat(size7d);
    }

    public void setSize(Broker broker){
        this.size1d = broker.size1d;
        this.size2d = broker.size2d;
        this.size3d = broker.size3d;
        this.size5d = broker.size5d;
        this.size7d = broker.size7d;
    }

    @Override
    public String toString() {
        return "Broker{" +
                "brokerID=" + brokerID +
                ", brokerName='" + brokerName + '\'' +
                ", maxOffset=" + maxOffset +
                ", writable=" + writable +
                ", size1d=" + size1d +
                ", size2d=" + size2d +
                ", size3d=" + size3d +
                ", size5d=" + size5d +
                ", size7d=" + size7d +
                "} " + super.toString();
    }
}
