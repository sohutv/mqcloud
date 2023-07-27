package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import org.apache.rocketmq.remoting.protocol.body.BrokerStatsData;

import com.sohu.tv.mq.cloud.util.WebUtil;
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
        return WebUtil.countFormat(getCount());
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
        return WebUtil.sizeFormat(getSize());
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
