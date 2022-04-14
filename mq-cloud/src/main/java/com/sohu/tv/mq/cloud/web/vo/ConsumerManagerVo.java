package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.util.WebUtil;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: TODO
 * @date 2022/2/28 15:20
 */
public class ConsumerManagerVo {

    //序号
    private int index;

    //当前关联Topic
    private Topic topic;

    //消费者
    private Consumer consumer;

    //前5分钟消费流量
    private String lastFiveMinusConsumerFlow;

    public ConsumerManagerVo() {
    }

    public ConsumerManagerVo(int index, Topic topic, Consumer consumer, Long lastFiveMinusConsumerFlow, String warmMessage) {
        this.index = index;
        this.topic = topic;
        this.consumer = consumer;
        this.lastFiveMinusConsumerFlow = WebUtil.countFormat(lastFiveMinusConsumerFlow);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public String getLastFiveMinusConsumerFlow() {
        return lastFiveMinusConsumerFlow;
    }

    public void setLastFiveMinusConsumerFlow(Long lastFiveMinusConsumerFlow) {
        this.lastFiveMinusConsumerFlow = WebUtil.countFormat(lastFiveMinusConsumerFlow);
    }
}
