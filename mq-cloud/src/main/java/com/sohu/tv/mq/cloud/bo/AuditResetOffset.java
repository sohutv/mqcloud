package com.sohu.tv.mq.cloud.bo;

public class AuditResetOffset {
    // 审核id
    private long aid;
    // 审核id
    private long tid;
    // 消费者id
    private long consumerId;
    // null:重置为最大offset,时间戳:重置为某时间点(yyyy-MM-dd#HH:mm:ss:SSS)
    private String offset;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(long consumerId) {
        this.consumerId = consumerId;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "AuditResetOffset [aid=" + aid + ", tid=" + tid + ", consumerId=" + consumerId + ", offset=" + offset
                + "]";
    }
}
