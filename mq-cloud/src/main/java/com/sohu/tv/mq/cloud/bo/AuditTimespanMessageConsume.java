package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import com.sohu.tv.mq.cloud.util.DateUtil;

/**
 * 时间段消息消费
 * 
 * @author yongfeigao
 * @date 2021年11月24日
 */
public class AuditTimespanMessageConsume {
    private long aid;
    // 从哪个topic拉取消息
    private String topic;
    // 消费者
    private String consumer;
    // 消费实例
    private String clientId;
    // 开始时间戳
    private long start;
    // 结束时间戳
    private long end;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
    
    public String getStartFormat() {
        return DateUtil.getFormat(DateUtil.YMD_BLANK_HMS_COLON).format(new Date(start));
    }
    
    public String getEndFormat() {
        return DateUtil.getFormat(DateUtil.YMD_BLANK_HMS_COLON).format(new Date(end));
    }

    @Override
    public String toString() {
        return "AuditTimespanMessageConsume [aid=" + aid + ", topic=" + topic + ", consumer=" + consumer + ", clientId="
                + clientId + ", start=" + start + ", end=" + end + "]";
    }
}
