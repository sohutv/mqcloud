package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.UserProducer;
/**
 * topic删除申请
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
public class AuditTopicDeleteVO {
    // topic
    private Topic topic;
    // topic生产者
    private List<UserProducer> userProducerList;
    
    private long aid;
    
    public Topic getTopic() {
        return topic;
    }
    public void setTopic(Topic topic) {
        this.topic = topic;
    }
    public List<UserProducer> getUserProducerList() {
        return userProducerList;
    }
    public void setUserProducerList(List<UserProducer> userProducerList) {
        this.userProducerList = userProducerList;
    }
    public long getAid() {
        return aid;
    }
    public void setAid(long aid) {
        this.aid = aid;
    }
}
