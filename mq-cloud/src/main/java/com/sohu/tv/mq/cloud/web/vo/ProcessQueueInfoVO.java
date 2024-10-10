package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.common.util.WebUtil;

import java.util.Date;

/**
 * @Auther: yongfeigao
 * @Date: 2023/9/8
 */
public class ProcessQueueInfoVO {
    public static final long ONE_YEAR_IN_MILLIS = 365 * 24 * 60 * 60 * 1000L;
    private long commitOffset;

    private long cachedMsgMinOffset;
    private long cachedMsgMaxOffset;
    private int cachedMsgCount;
    private int cachedMsgSizeInMiB;

    private long transactionMsgMinOffset;
    private long transactionMsgMaxOffset;
    private int transactionMsgCount;

    private boolean locked;
    private long tryUnlockTimes;
    private long lastLockTimestamp;

    private boolean droped;
    private long lastPullTimestamp;
    private long lastConsumeTimestamp;

    private long brokerOffset;

    private int pullThresholdForQueue;
    private int pullThresholdSizeForQueue;
    private int consumeConcurrentlyMaxSpan;
    private boolean orderConsumer;

    public ProcessQueueInfoVO(ConsumerRunningInfoVO consumerRunningInfo) {
        if (consumerRunningInfo.getProperties().size() <= 0) {
            return;
        }
        pullThresholdForQueue = consumerRunningInfo.getPullThresholdForQueue();
        pullThresholdSizeForQueue = consumerRunningInfo.getPullThresholdSizeForQueue();
        consumeConcurrentlyMaxSpan = consumerRunningInfo.getConsumeConcurrentlyMaxSpan();
        orderConsumer = consumerRunningInfo.isConsumeOrderly();
    }

    public long getCommitOffset() {
        return commitOffset;
    }

    public void setCommitOffset(long commitOffset) {
        this.commitOffset = commitOffset;
    }

    public long getCachedMsgMinOffset() {
        return cachedMsgMinOffset;
    }

    public void setCachedMsgMinOffset(long cachedMsgMinOffset) {
        this.cachedMsgMinOffset = cachedMsgMinOffset;
    }

    public long getCachedMsgMaxOffset() {
        return cachedMsgMaxOffset;
    }

    public void setCachedMsgMaxOffset(long cachedMsgMaxOffset) {
        this.cachedMsgMaxOffset = cachedMsgMaxOffset;
    }

    public int getCachedMsgCount() {
        return cachedMsgCount;
    }

    public void setCachedMsgCount(int cachedMsgCount) {
        this.cachedMsgCount = cachedMsgCount;
    }

    public int getCachedMsgSizeInMiB() {
        return cachedMsgSizeInMiB;
    }

    public void setCachedMsgSizeInMiB(int cachedMsgSizeInMiB) {
        this.cachedMsgSizeInMiB = cachedMsgSizeInMiB;
    }

    public long getTransactionMsgMinOffset() {
        return transactionMsgMinOffset;
    }

    public void setTransactionMsgMinOffset(long transactionMsgMinOffset) {
        this.transactionMsgMinOffset = transactionMsgMinOffset;
    }

    public long getTransactionMsgMaxOffset() {
        return transactionMsgMaxOffset;
    }

    public void setTransactionMsgMaxOffset(long transactionMsgMaxOffset) {
        this.transactionMsgMaxOffset = transactionMsgMaxOffset;
    }

    public int getTransactionMsgCount() {
        return transactionMsgCount;
    }

    public void setTransactionMsgCount(int transactionMsgCount) {
        this.transactionMsgCount = transactionMsgCount;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public long getTryUnlockTimes() {
        return tryUnlockTimes;
    }

    public void setTryUnlockTimes(long tryUnlockTimes) {
        this.tryUnlockTimes = tryUnlockTimes;
    }

    public void setLastLockTimestamp(long lastLockTimestamp) {
        this.lastLockTimestamp = lastLockTimestamp;
    }

    public boolean isDroped() {
        return droped;
    }

    public void setDroped(boolean droped) {
        this.droped = droped;
    }

    public void setLastPullTimestamp(long lastPullTimestamp) {
        this.lastPullTimestamp = lastPullTimestamp;
    }

    public void setLastConsumeTimestamp(long lastConsumeTimestamp) {
        this.lastConsumeTimestamp = lastConsumeTimestamp;
    }

    public String getLastLockTimestampFormat() {
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(new Date(lastLockTimestamp));
    }

    public String getLastPullTimestampFormat() {
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(new Date(lastPullTimestamp));
    }


    public String getLastConsumeTimestampFormat() {
        long now = System.currentTimeMillis();
        if (lastConsumeTimestamp > now) {
            return "暂无";
        }
        if ((now - lastConsumeTimestamp) > ONE_YEAR_IN_MILLIS) {
            return "暂无";
        }
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(new Date(lastConsumeTimestamp));
    }

    public long getMaxSpan() {
        return cachedMsgMaxOffset - cachedMsgMinOffset;
    }

    public int getPullThresholdForQueue() {
        return pullThresholdForQueue;
    }

    public void setPullThresholdForQueue(int pullThresholdForQueue) {
        this.pullThresholdForQueue = pullThresholdForQueue;
    }

    public int getPullThresholdSizeForQueue() {
        return pullThresholdSizeForQueue;
    }

    public void setPullThresholdSizeForQueue(int pullThresholdSizeForQueue) {
        this.pullThresholdSizeForQueue = pullThresholdSizeForQueue;
    }

    public int getConsumeConcurrentlyMaxSpan() {
        return consumeConcurrentlyMaxSpan;
    }

    public void setConsumeConcurrentlyMaxSpan(int consumeConcurrentlyMaxSpan) {
        this.consumeConcurrentlyMaxSpan = consumeConcurrentlyMaxSpan;
    }

    public boolean isOrderConsumer() {
        return orderConsumer;
    }

    public void setOrderConsumer(boolean orderConsumer) {
        this.orderConsumer = orderConsumer;
    }

    public boolean isLastPullLate() {
        return (System.currentTimeMillis() - lastPullTimestamp) > 1000 * 60 * 5;
    }

    public boolean isCachedMsgCountOverThreshold() {
        return cachedMsgCount > pullThresholdForQueue;
    }

    public boolean isCachedMsgSizeOverThreshold() {
        return cachedMsgSizeInMiB > pullThresholdSizeForQueue;
    }

    public boolean isMaxSpanOverThreshold() {
        return !orderConsumer && cachedMsgMinOffset > 0 && getMaxSpan() > consumeConcurrentlyMaxSpan;
    }

    public String getAccumulationCount() {
        if (commitOffset >= 0 && brokerOffset > commitOffset) {
            return WebUtil.countFormat(brokerOffset - commitOffset);
        }
        return "0";
    }

    public long getLastPullTimestamp() {
        return lastPullTimestamp;
    }

    public long getLastLockTimestamp() {
        return lastLockTimestamp;
    }

    public long getLastConsumeTimestamp() {
        return lastConsumeTimestamp;
    }

    public long getBrokerOffset() {
        return brokerOffset;
    }

    public void setBrokerOffset(long brokerOffset) {
        this.brokerOffset = brokerOffset;
    }
}
