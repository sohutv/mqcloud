package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * 消息重置
 * 
 * @author yongfeigao
 * @date 2019年10月28日
 */
public class MessageReset {
    // 消费者
    private String consumer;
    // 重置到
    private long resetTo;
    // 更新时间
    private Date updateTime;

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public long getResetTo() {
        return resetTo;
    }

    public void setResetTo(long resetTo) {
        this.resetTo = resetTo;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "MessageReset [consumer=" + consumer + ", resetTo=" + resetTo + ", updateTime=" + updateTime + "]";
    }
}
