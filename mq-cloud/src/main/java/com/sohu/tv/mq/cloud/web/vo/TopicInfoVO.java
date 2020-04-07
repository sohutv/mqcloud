package com.sohu.tv.mq.cloud.web.vo;

import java.util.ArrayList;
import java.util.List;

import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.UserProducer;

/**
 * topic 生产者和消费者vo
 * 
 * @author yongfeigao
 * @date 2020年3月17日
 */
public class TopicInfoVO {
    private Topic topic;
    private List<Consumer> consumerList = new ArrayList<>();
    private List<UserProducer> producerList = new ArrayList<>();

    public void addConsumer(Consumer consumer) {
        if (consumer == null) {
            return;
        }
        consumerList.add(consumer);
    }

    public void addUserProducer(UserProducer userProducer) {
        if (userProducer == null) {
            return;
        }
        producerList.add(userProducer);
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public List<Consumer> getConsumerList() {
        return consumerList;
    }

    public void setConsumerList(List<Consumer> consumerList) {
        this.consumerList = consumerList;
    }

    public List<UserProducer> getProducerList() {
        return producerList;
    }

    public void setProducerList(List<UserProducer> producerList) {
        this.producerList = producerList;
    }
}
