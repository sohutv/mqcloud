package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.common.util.WebUtil;

import java.util.Date;

/**
 * 各个队列消费阻塞情况
 * 
 * @author yongfeigao
 */
public class ConsumerBlock {
    // 消费情况id
    private int csid;
    // client id
    private String instance;
    // broker name
    private String broker;
    // queue id
    private int qid;
    // 阻塞时间,当前时间-最新消费时间 ms
    private long blockTime;
    // 记录更新时间
    private Date updatetime;
    // 最近一次发生offset moved事件的时间
    private long offsetMovedTime;
    // 发生offset moved事件的次数
    private int offsetMovedTimes;

    public long getOffsetMovedTime() {
        return offsetMovedTime;
    }

    public void setOffsetMovedTime(long offsetMovedTime) {
        this.offsetMovedTime = offsetMovedTime;
    }

    public int getOffsetMovedTimes() {
        return offsetMovedTimes;
    }

    public void setOffsetMovedTimes(int offsetMovedTimes) {
        this.offsetMovedTimes = offsetMovedTimes;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(long blockTime) {
        this.blockTime = blockTime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public int getCsid() {
        return csid;
    }

    public void setCsid(int csid) {
        this.csid = csid;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public int getQid() {
        return qid;
    }

    public void setQid(int qid) {
        this.qid = qid;
    }

    public String getBlockTimeFormat() {
        return WebUtil.timeFormat(blockTime);
    }

    @Override
    public String toString() {
        return "ConsumerBlock [csid=" + csid + ", instance=" + instance
                + ", broker=" + broker + ", qid=" + qid + ", blockTime="
                + blockTime + ", updatetime=" + updatetime + "]";
    }
}
