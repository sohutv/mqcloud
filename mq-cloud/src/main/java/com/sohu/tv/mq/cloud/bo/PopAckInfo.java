package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.util.DateUtil;

import java.util.Date;

public class PopAckInfo {
    private String brokerName;
    private int queueId;
    private long offset;
    private long nextOffset;
    private long lockTime;
    private boolean forceAck;
    private long expireTime;
    private String requestId;
    private int retryTimes;
    private String clientIp;

    public int getMsgSize() {
        return (int) (nextOffset - offset);
    }

    public String getLockTimeFormat() {
        if (lockTime <= 0) {
            return null;
        }
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(new Date(lockTime));
    }

    public String getExpireTimeFormat() {
        if (expireTime <= 0) {
            return null;
        }
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(new Date(expireTime));
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(long nextOffset) {
        this.nextOffset = nextOffset;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public boolean isForceAck() {
        return forceAck;
    }

    public void setForceAck(boolean forceAck) {
        this.forceAck = forceAck;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
