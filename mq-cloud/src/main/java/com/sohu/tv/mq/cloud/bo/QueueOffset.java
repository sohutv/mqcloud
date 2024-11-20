package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 消费者队列偏移量
 *
 * @author: yongfeigao
 * @date: 2022/5/30 9:50
 */
public class QueueOffset {
    private MessageQueue messageQueue;
    private long maxOffset;
    private long committedOffset = -1;
    private long lockTimestamp;
    private long lastConsumeTimestamp;
    private String lastConsumeClientIp;

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public long getCommittedOffset() {
        return committedOffset;
    }

    public void setCommittedOffset(long committedOffset) {
        this.committedOffset = committedOffset;
    }

    public long getLockTimestamp() {
        return lockTimestamp;
    }

    public void setLockTimestamp(long lockTimestamp) {
        this.lockTimestamp = lockTimestamp;
    }

    public long getLastConsumeTimestamp() {
        return lastConsumeTimestamp;
    }

    public void setLastConsumeTimestamp(long lastConsumeTimestamp) {
        this.lastConsumeTimestamp = lastConsumeTimestamp;
    }

    public String getLastConsumeClientIp() {
        return lastConsumeClientIp;
    }

    public void setLastConsumeClientIp(String lastConsumeClientIp) {
        this.lastConsumeClientIp = lastConsumeClientIp;
    }
}
