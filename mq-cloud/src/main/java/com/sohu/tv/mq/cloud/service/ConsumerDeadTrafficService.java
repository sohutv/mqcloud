package com.sohu.tv.mq.cloud.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.TopicConsumer;
import com.sohu.tv.mq.cloud.bo.TopicHourTraffic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;

/**
 * 消费者死消息监控
 * 
 * @author yongfeigao
 * @date 2020年7月9日
 */
@Service
public class ConsumerDeadTrafficService extends HourTrafficService {

    @Autowired
    private AlertService alertService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    protected void alert(TopicHourTraffic topicTraffic, TopicConsumer topicConsumer, List<User> userList) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("topic", topicConsumer.getTopic());
        paramMap.put("consumer", mqCloudConfigHelper.getTopicConsumeLink(topicConsumer.getTid(), topicConsumer.getConsumer()));
        paramMap.put("count", topicTraffic.getCount());
        paramMap.put("resource", topicConsumer.getConsumer());
        alertService.sendWarn(userList, WarnType.DEAD_MESSAGE, paramMap);
    }

    protected String getCountKey() {
        return "DEAD_PUT_NUMS";
    }
}
