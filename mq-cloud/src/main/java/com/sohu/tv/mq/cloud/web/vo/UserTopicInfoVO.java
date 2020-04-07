package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.User;

/**
 * 用户和topic 生产者和消费者vo
 * 
 * @author yongfeigao
 * @date 2020年3月18日
 */
public class UserTopicInfoVO {
    // 审核id
    private long aid;
    private List<User> userList;
    private List<TopicInfoVO> topicInfoVoList;

    public UserTopicInfoVO() {

    }

    public UserTopicInfoVO(List<User> userList, List<TopicInfoVO> topicInfoVoList) {
        this.userList = userList;
        this.topicInfoVoList = topicInfoVoList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public List<TopicInfoVO> getTopicInfoVoList() {
        return topicInfoVoList;
    }

    public void setTopicInfoVoList(List<TopicInfoVO> topicInfoVoList) {
        this.topicInfoVoList = topicInfoVoList;
    }

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }
}
