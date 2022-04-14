package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * 时间段消息消费
 * 
 * @author yongfeigao
 * @date 2021年11月24日
 */
public class TimespanMessageConsumeParam {
    // 从哪个topic拉取消息
    @NotBlank
    private String topic;
    // 消费者
    @Range(min = 1)
    private long consumerId;
    // 消费实例
    @NotBlank
    private String clientId;
    // 开始时间戳
    @Range(min = 1)
    private long start;
    // 结束时间戳
    @Range(min = 1)
    private long end;
    // 消费者
    private String consumer;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(long consumerId) {
        this.consumerId = consumerId;
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

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }
}
