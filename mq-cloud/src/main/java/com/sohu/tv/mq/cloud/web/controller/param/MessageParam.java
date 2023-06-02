package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * 消息参数
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月24日
 */
public class MessageParam {
    @Range(min = 1)
    private int cid;
    @NotBlank
    private String topic;
    @Range(min = 1)
    private long tid;
    @Range(min = 0)
    private int qid;
    @NotBlank
    private String broker;
    @Range(min = 0)
    private long offset;
    // 消息id
    private String msgId;
    public int getCid() {
        return cid;
    }
    public void setCid(int cid) {
        this.cid = cid;
    }
    public long getTid() {
        return tid;
    }
    public void setTid(long tid) {
        this.tid = tid;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public int getQueueId() {
        return qid;
    }
    public void setQueueId(int queueId) {
        this.qid = queueId;
    }
    public String getStoreHost() {
        return broker;
    }
    public void setStoreHost(String storeHost) {
        this.broker = storeHost;
    }
    public long getQueueOffset() {
        return offset;
    }
    public void setQueueOffset(long queueOffset) {
        this.offset = queueOffset;
    }
    
    public void setQid(int qid) {
        setQueueId(qid);
    }
    
    public void setOffset(long offset) {
        setQueueOffset(offset);
    }
    
    public void setBroker(String broker) {
        setStoreHost(broker);
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
