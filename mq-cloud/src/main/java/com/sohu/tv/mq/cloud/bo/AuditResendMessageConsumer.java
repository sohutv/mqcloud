package com.sohu.tv.mq.cloud.bo;

/**
 * 重发消息给消费者
 * 
 * @author yongfeigao
 * @date 2019年11月5日
 */
public class AuditResendMessageConsumer {
    private long aid;
    private long consumerId;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(long consumerId) {
        this.consumerId = consumerId;
    }
}
