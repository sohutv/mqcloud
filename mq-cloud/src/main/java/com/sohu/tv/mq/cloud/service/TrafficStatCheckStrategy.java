package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.bo.TopicTrafficCheckResult;
import com.sohu.tv.mq.cloud.bo.TopicTrafficStat;

import java.util.List;

/**
 * @author yongweizhao
 * @create 2020/9/27 16:22
 */
public interface TrafficStatCheckStrategy {

    public TopicTrafficStat stat();

    public TopicTrafficCheckResult check(TopicTraffic topicTraffic);

    public List<TopicTrafficCheckResult> check(List<TopicTraffic> topicTrafficList);
}
