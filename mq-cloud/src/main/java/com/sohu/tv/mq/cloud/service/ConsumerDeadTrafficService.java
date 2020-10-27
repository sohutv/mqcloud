package com.sohu.tv.mq.cloud.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.TopicConsumer;
import com.sohu.tv.mq.cloud.bo.TopicHourTraffic;
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

    protected void alert(TopicHourTraffic topicTraffic, TopicConsumer topicConsumer, String email) {
        alertService.sendWarnMail(email, "死消费", "topic:<b>" + topicConsumer.getTopic() + "</b> 消费者:<b>"
                + mqCloudConfigHelper.getTopicConsumeLink(topicConsumer.getTid(), topicConsumer.getConsumer())
                + "</b> 死消息量:" + topicTraffic.getCount());
    }

    protected String getCountKey() {
        return "DEAD_PUT_NUMS";
    }
}
