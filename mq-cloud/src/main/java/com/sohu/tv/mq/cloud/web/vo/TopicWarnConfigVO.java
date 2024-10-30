package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicWarnConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * topic预警配置VO
 *
 * @author yongfeigao
 * @date 2024年10月11日
 */
public class TopicWarnConfigVO {

    private Topic topic;

    private List<TopicWarnConfig> topicWarnConfigs;

    public List<TopicWarnConfig> getTopicWarnConfigs() {
        return topicWarnConfigs;
    }

    public void setTopicWarnConfigs(List<TopicWarnConfig> topicWarnConfigs) {
        this.topicWarnConfigs = topicWarnConfigs;
    }

    public void addTopicWarnConfig(TopicWarnConfig topicWarnConfig) {
        if (topicWarnConfigs == null) {
            topicWarnConfigs = new ArrayList<>();
        }
        topicWarnConfigs.add(topicWarnConfig);
    }

    public int getTopicWarnConfigSize() {
        return topicWarnConfigs.size();
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }
}
