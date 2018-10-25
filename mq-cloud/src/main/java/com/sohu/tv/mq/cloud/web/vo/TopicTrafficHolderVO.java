package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;
/**
 * topic流量列表持有者
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月9日
 */
public class TopicTrafficHolderVO {
    // topic查询参数
    private String queryTopic;
    
    // topic流量列表
    private List<TopicTrafficVO> topicTrafficVOList;
    
    public String getQueryTopic() {
        return queryTopic;
    }

    public void setQueryTopic(String queryTopic) {
        this.queryTopic = queryTopic;
    }

    public List<TopicTrafficVO> getTopicTrafficVOList() {
        return topicTrafficVOList;
    }

    public void setTopicTrafficVOList(List<TopicTrafficVO> topicTrafficVOList) {
        this.topicTrafficVOList = topicTrafficVOList;
    }
}
