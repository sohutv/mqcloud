package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.util.WebUtil;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: TODO
 * @date 2022/2/13 14:59
 */
public class TopicManagerInfoVo {

    private Integer index;

    // 当前Topic明细信息
    private Topic topic;

    //前5分钟生产流量
    private String lastFiveMinusProducerFlow;

    //前5分钟消费流量
    private String lastFiveMinusConsumerFlow;

    public TopicManagerInfoVo() {
    }

    public TopicManagerInfoVo(Integer index, Topic topic, Long lastFiveMinusProducerFlow, Long lastFiveMinusConsumerFlow) {
        this.index = index;
        this.topic = topic;
        this.lastFiveMinusProducerFlow = WebUtil.countFormat(lastFiveMinusProducerFlow);
        this.lastFiveMinusConsumerFlow = WebUtil.countFormat(lastFiveMinusConsumerFlow);
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public String getLastFiveMinusProducerFlow() {
        return lastFiveMinusProducerFlow;
    }

    public void setLastFiveMinusProducerFlow(Long lastFiveMinusProducerFlow) {
        this.lastFiveMinusProducerFlow = WebUtil.countFormat(lastFiveMinusProducerFlow);
    }

    public String getLastFiveMinusConsumerFlow() {
        return lastFiveMinusConsumerFlow;
    }

    public void setLastFiveMinusConsumerFlow(Long lastFiveMinusConsumerFlow) {
        this.lastFiveMinusConsumerFlow = WebUtil.countFormat(lastFiveMinusConsumerFlow);
    }
}
