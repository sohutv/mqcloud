package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.Topic;

/**
 * topic路由
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月10日
 */
public class TopicRouteVO {
    private Topic topic;
    private int queueNum = 8;
    private List<TopicRoute> topicRouteList;
    private boolean own;
    public int getQueueNum() {
        return queueNum;
    }
    public void setQueueNum(int queueNum) {
        this.queueNum = queueNum;
    }
    public Topic getTopic() {
        return topic;
    }
    public void setTopic(Topic topic) {
        this.topic = topic;
    }
    public List<TopicRoute> getTopicRouteList() {
        return topicRouteList;
    }
    public void setTopicRouteList(List<TopicRoute> topicRouteList) {
        this.topicRouteList = topicRouteList;
    }
    public boolean isOwn() {
        return own;
    }
    public void setOwn(boolean own) {
        this.own = own;
    }
}
