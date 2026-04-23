package com.sohu.tv.mq.cloud.web.controller.param;

import com.sohu.tv.mq.util.MQProtocol;
import org.hibernate.validator.constraints.Range;

public class UserConsumerParam {
    @Range(min = 1)
    private long tid;
    @Range(min = 1)
    private long consumerId;
    
    private String offset;

    private long cid;
    
    private String messageKey;

    private String topic;

    private String consumer;

    private int protocol;

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

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public boolean isHttpProtocol() {
        return protocol == MQProtocol.HTTP.getType();
    }

    @Override
    public String toString() {
        return "UserConsumerParam [tid=" + tid + ", consumerId=" + consumerId + ", offset=" + offset + ", cid=" + cid
                + ", messageKey=" + messageKey + "]";
    }
}
