package com.sohu.tv.mq.cloud.bo;

import java.util.List;
import java.util.Map;

/**
 * topic 拓扑
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月13日
 */
public class TopicTopology {
    // 用户id
    private long uid;
    // topic对象
    private Topic topic;
    // consumer列表
    private List<Consumer> consumerList;
    // 按照producer group分类后的map
    private Map<StatsProducer, List<UserProducer>> producerFilterMap;

    // producer列表
    private List<UserProducer> prevProducerList;

    // topic 流量
    private Traffic topicTraffic;

    private int brokerSize;
    // 是否是拥有者
    private boolean own;
    // 总流量
    private TopicTraffic totalTopicTraffic;
    // 总流量
    private ConsumerTraffic totalConsumerTraffic;
    
    // 生产者是否具有流量
    private boolean producerHasTraffic;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
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

    public Traffic getTopicTraffic() {
        return topicTraffic;
    }

    public void setTopicTraffic(Traffic topicTraffic) {
        this.topicTraffic = topicTraffic;
    }

    public List<UserProducer> getPrevProducerList() {
        return prevProducerList;
    }

    public void setPrevProducerList(List<UserProducer> prevProducerList) {
        this.prevProducerList = prevProducerList;
    }

    public int getBrokerSize() {
        return brokerSize;
    }

    public void setBrokerSize(int brokerSize) {
        this.brokerSize = brokerSize;
    }

    public boolean isOwn() {
        return own;
    }

    public void setOwn(boolean own) {
        this.own = own;
    }

    public TopicTraffic getTotalTopicTraffic() {
        return totalTopicTraffic;
    }

    public void setTotalTopicTraffic(TopicTraffic totalTopicTraffic) {
        this.totalTopicTraffic = totalTopicTraffic;
    }

    public ConsumerTraffic getTotalConsumerTraffic() {
        return totalConsumerTraffic;
    }

    public void setTotalConsumerTraffic(ConsumerTraffic totalConsumerTraffic) {
        this.totalConsumerTraffic = totalConsumerTraffic;
    }

    public Map<StatsProducer, List<UserProducer>> getProducerFilterMap() {
        return producerFilterMap;
    }

    public void setProducerFilterMap(Map<StatsProducer, List<UserProducer>> producerFilterMap) {
        this.producerFilterMap = producerFilterMap;
    }

    public boolean isProducerHasTraffic() {
        return producerHasTraffic;
    }

    public void setProducerHasTraffic(boolean producerHasTraffic) {
        this.producerHasTraffic = producerHasTraffic;
    }
}
