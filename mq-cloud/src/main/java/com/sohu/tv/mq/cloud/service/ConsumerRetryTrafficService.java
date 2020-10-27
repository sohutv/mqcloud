package com.sohu.tv.mq.cloud.service;

import org.apache.rocketmq.store.stats.BrokerStatsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.TopicConsumer;
import com.sohu.tv.mq.cloud.bo.TopicHourTraffic;
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
    
    protected void alert(TopicHourTraffic topicTraffic, TopicConsumer topicConsumer, String email) {
        long consumerFailCount = alarmConfigBridingService.getConsumerFailCount(topicConsumer.getConsumer());
        if (consumerFailCount >= 0 && consumerFailCount < topicTraffic.getCount()) {
            // 验证报警频率
            if (alarmConfigBridingService.needWarn("consumerFail", topicConsumer.getTopic(), topicConsumer.getConsumer())) {
                alertService.sendWarnMail(email, "消费失败", "topic:<b>" + topicConsumer.getTopic() + "</b> 消费者:<b>"
                        + mqCloudConfigHelper.getTopicConsumeLink(topicConsumer.getTid(), topicConsumer.getConsumer())
                        + "</b> 消费失败量:" + topicTraffic.getCount());
            }
        }
    }

    protected String getCountKey() {
        return BrokerStatsManager.SNDBCK_PUT_NUMS;
    }
}
