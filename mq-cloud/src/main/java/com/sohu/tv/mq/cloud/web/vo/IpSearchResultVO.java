package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.Topic;

/**
 * ip搜索结果VO
 * @author yongweizhao
 * @create 2019/11/8 11:35
 */
public class IpSearchResultVO {
    // ip
    private String ip;
    // consumer
    private String consumer;
    // producer
    private String producer;
    // topic
    private Topic topic;

    public IpSearchResultVO() {}

    public IpSearchResultVO(String ip, String consumer, String producer, Topic topic) {
        this.ip = ip;
        this.consumer = consumer;
        this.producer = producer;
        this.topic = topic;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "IpSearchResultVO{" +
                "ip='" + ip + '\'' +
                ", consumer='" + consumer + '\'' +
                ", producer='" + producer + '\'' +
                ", topic=" + topic +
                '}';
    }
}
