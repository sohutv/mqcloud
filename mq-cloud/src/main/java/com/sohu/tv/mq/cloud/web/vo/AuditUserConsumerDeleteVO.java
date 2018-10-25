package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;

/**
 * UserConsumer删除申请
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月7日
 */
public class AuditUserConsumerDeleteVO {
    // topic
    private Topic topic;
    // 用户
    private User user;
    // 消费者
    private Consumer consumer;
    //用于前台判断是否已经处理过
    private boolean isCommit;

    private long aid;
    // 与当前消费者关联的所有用户
    private List<User> userList;

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public boolean isCommit() {
        return isCommit;
    }

    public void setCommit(boolean isCommit) {
        this.isCommit = isCommit;
    }
}
