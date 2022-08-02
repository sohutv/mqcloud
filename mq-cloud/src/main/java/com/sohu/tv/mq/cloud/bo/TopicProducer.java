package com.sohu.tv.mq.cloud.bo;

/**
 * topic消费者
 * @author: yongfeigao
 * @date: 2022/6/23 14:24
 */
public class TopicProducer {
    private String topic;
    private String producer;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }
}
