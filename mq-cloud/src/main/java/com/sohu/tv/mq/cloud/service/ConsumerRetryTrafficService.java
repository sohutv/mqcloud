package com.sohu.tv.mq.cloud.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.store.stats.BrokerStatsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.TopicConsumer;
import com.sohu.tv.mq.cloud.bo.TopicHourTraffic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;

/**
 * 消费者重试消息监控
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月26日
 */
@Service
public class ConsumerRetryTrafficService extends HourTrafficService{

    @Autowired
    private AlertService alertService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private AlarmConfigBridingService alarmConfigBridingService;
    
    protected void alert(TopicHourTraffic topicTraffic, TopicConsumer topicConsumer, List<User> userList) {
        long consumerFailCount = alarmConfigBridingService.getConsumerFailCount(topicConsumer.getConsumer());
        if (consumerFailCount >= 0 && consumerFailCount < topicTraffic.getCount()) {
            // 验证报警频率
            if (alarmConfigBridingService.needWarn("consumerFail", topicConsumer.getTopic(), topicConsumer.getConsumer())) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("topic", topicConsumer.getTopic());
                paramMap.put("consumer",mqCloudConfigHelper.getTopicConsumeLink(topicConsumer.getTid(), 
                        topicConsumer.getConsumer(), System.currentTimeMillis()));
                paramMap.put("count", topicTraffic.getCount());
                paramMap.put("link", mqCloudConfigHelper.getTopicConsumeHref(topicConsumer.getTid(),
                        topicConsumer.getConsumer(), topicConsumer.getCid(), 0));
                paramMap.put("resource", topicConsumer.getConsumer());
                alertService.sendWarn(userList, WarnType.CONSUME_FAIL, paramMap);
            }
        }
    }

    protected String getCountKey() {
        return BrokerStatsManager.SNDBCK_PUT_NUMS;
    }
}
