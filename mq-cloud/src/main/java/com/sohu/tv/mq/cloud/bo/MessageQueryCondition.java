package com.sohu.tv.mq.cloud.bo;

import java.io.IOException;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.cloud.util.CompressUtil;

/**
 * 消息查询条件
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年8月21日
 */
public class MessageQueryCondition {
    public static final int MESSAGE_SIZE = 30;
    public static final int MAX_MESSAGE_SIZE = 5000;
    private List<MQOffset> mqOffsetList;
    // topic
    private String topic;
    // 集群id
    private int cid;
    // 关键字
    private String key;
    // 查询时间
    private long start;
    // 查询时间
    private long end;
    // 剩余的消息数
    private long leftSize;
    // 本次检索的消息数
    private long searchedSize;
    // 上次命中的消息数
    private long prevSize;
    // 本次命中的消息数
    private long curSize;
    // 查询的次数
    private long times;
    // 最大偏移量
    private long maxOffset;
    // brokerName
    private String brokerName;
    // 队列id
    private Integer queueId;

    public List<MQOffset> getMqOffsetList() {
        return mqOffsetList;
    }

    public void setMqOffsetList(List<MQOffset> mqOffsetList) {
        this.mqOffsetList = mqOffsetList;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean valid(long time) {
        if (time >= start && time <= end) {
            return true;
        }
        return false;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public long getLeftSize() {
        return leftSize;
    }

    public void setLeftSize(long leftSize) {
        this.leftSize = leftSize;
    }

    /**
     * 计算剩余量
     */
    public void calculateLeftSize() {
        long leftSize = 0;
        for (MQOffset mqOffset : getMqOffsetList()) {
            long size = mqOffset.getMaxOffset() - mqOffset.getOffset();
            if (size > 0) {
                leftSize += size;
            }
        }
        setLeftSize(leftSize);
    }

    /**
     * 搜索量达到最大值
     * 
     * @return
     */
    public boolean reachMaxCount() {
        return getSearchedSize() >= MessageQueryCondition.MAX_MESSAGE_SIZE;
    }

    /**
     * 搜索量达到既定量
     * 
     * @return
     */
    public boolean reachCount() {
        return getCurSize() >= MessageQueryCondition.MESSAGE_SIZE;
    }

    /**
     * 是否需要搜索
     * 
     * @return
     */
    public boolean needSearch() {
        return !reachCount() && !reachMaxCount();
    }

    public long getSearchedSize() {
        return searchedSize;
    }

    public void setSearchedSize(long searchedSize) {
        this.searchedSize = searchedSize;
    }

    public long getPrevSize() {
        return prevSize;
    }

    public void setPrevSize(long prevSize) {
        this.prevSize = prevSize;
    }

    public long getCurSize() {
        return curSize;
    }

    public void setCurSize(long curSize) {
        this.curSize = curSize;
    }

    public String serialize() throws IOException {
        return CompressUtil.compress(JSON.toJSONString(this));
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }
    
    /**
     * 为搜索做准备
     */
    public void prepareForSearch() {
        // 设置查询次数
        setTimes(getTimes() + 1);
        // 保存之前的记录数
        setPrevSize(getPrevSize() + getCurSize());
        // 设置本次查询量
        setCurSize(0);
        // 设置搜索量
        setSearchedSize(0);
    }

    public void reset() {
        setMqOffsetList(null);
        setTimes(1);
        setSearchedSize(0);
        setLeftSize(0);
        setCurSize(0);
        setPrevSize(0);
        setKey(null);
        setBrokerName(null);
        setQueueId(null);
    }

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }
    
    public long getMinOffset() {
        long minOffset = maxOffset - 32;
        return minOffset < 0 ? 0 : minOffset;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }
}
