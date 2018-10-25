package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.common.message.MessageQueue;
/**
 * 存储MQ的offset
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月21日
 */
public class MQOffset {
    private MessageQueue mq;
    private long minOffset;
    private long maxOffset;
    private long offset;
    public MessageQueue getMq() {
        return mq;
    }
    public void setMq(MessageQueue mq) {
        this.mq = mq;
    }
    public long getMinOffset() {
        return minOffset;
    }
    public void setMinOffset(long minOffset) {
        this.minOffset = minOffset;
    }
    public long getMaxOffset() {
        return maxOffset;
    }
    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }
    public long getOffset() {
        return offset;
    }
    public void setOffset(long offset) {
        this.offset = offset;
    }
    public boolean hasMessage() {
        return offset <= maxOffset && maxOffset > 0;
    }
    @Override
    public String toString() {
        return "MQOffset [mq=" + mq + ", minOffset=" + minOffset + ", maxOffset=" + maxOffset + ", offset=" + offset
                + "]";
    }
}
