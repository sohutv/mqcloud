package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
/**
 * consumer删除申请
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
public class AuditConsumerDeleteVO {
    // topic
    private Topic topic;
    // 用户
    private List<User> user;
    // 消费者
    private Consumer consumer;
    
    private long aid;
    
    public Topic getTopic() {
        return topic;
    }
    public void setTopic(Topic topic) {
        this.topic = topic;
    }
    public List<User> getUser() {
        return user;
    }
    public void setUser(List<User> user) {
        this.user = user;
    }
    public Consumer getConsumer() {
        return consumer;
    }
    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }
    public long getAid() {
        return aid;
    }
    public void setAid(long aid) {
        this.aid = aid;
    }
}
