package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserProducer;

/**
 * userProducer删除申请
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月5日
 */
public class AuditUserProducerDeleteVO {
    // topic
    private Topic topic;
    // 用户
    private User user;
    // 消费者
    private UserProducer userProducer;
    // 与当前生产者关联的所有用户
    private List<User> userList;

    // 是否已经处理过
    private boolean isCommit;

    private long aid;

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

    public UserProducer getUserProducer() {
        return userProducer;
    }

    public void setUserProducer(UserProducer userProducer) {
        this.userProducer = userProducer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
