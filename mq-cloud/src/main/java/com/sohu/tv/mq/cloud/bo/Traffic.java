package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import org.apache.rocketmq.common.protocol.body.BrokerStatsData;
/**
 * 流量
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月26日
 */
public class Traffic {
    // 创建日期
    private Date createDate;
    // 创建时间，格式：HHMM
    private String createTime;
    // 次数
    private long count;
    // 大小
    private long size;
    
    // 冗余字段
    private int clusterId;
    
    // 本次检测获取的量
    private long curCount;
    private long curSize;

    public long getCurCount() {
        return curCount;
    }

    public void setCurCount(long curCount) {
        this.curCount = curCount;
    }

    public long getCurSize() {
        return curSize;
    }

    public void setCurSize(long curSize) {
        this.curSize = curSize;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
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

    public long getCount() {
        return count;
    }
    
    public String getCountFormat() {
        if(getCount() >= 100000000) {
            return format(getCount() / 100000000F) + "亿";
        }
        if(getCount() >= 10000) {
            return format(getCount() / 10000F) + "万";
        }
        return String.valueOf(getCount());
    }

    public void setCount(long count) {
        this.count = count;
    }
    
    public void addCount(BrokerStatsData brokerPutStatsData) {
        addCount(brokerPutStatsData.getStatsMinute().getSum());
    }
    
    public void addCount(long count) {
        this.curCount = count;
        this.count += count;
    }

    public long getSize() {
        return size;
    }
    
    public String getSizeFormat() {
        if(getSize() >= 1073741824) {
            return format(getSize() / 1073741824F) + "g";
        }
        if(getSize() >= 1048576) {
            return format(getSize() / 1048576F) + "m";
        }
        if(getSize() >= 1024) {
            return format(getSize() / 1024F) + "k";
        }
        return getSize()+"b";
    }
    
    private String format(float value) {
        return String.valueOf(Math.round(value*100)/100.0);
    }

    public void setSize(long size) {
        this.size = size;
    }
    
    public void addSize(BrokerStatsData brokerSizeStatsData) {
        addSize(brokerSizeStatsData.getStatsMinute().getSum());
    }
    
    public void addSize(long size) {
        this.curSize = size;
        this.size += size;
    }

    @Override
    public String toString() {
        return "Traffic [createDate=" + createDate + ", createTime=" + createTime + ", count=" + count + ", size="
                + size + "]";
    }
    
}
