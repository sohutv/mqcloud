package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.bo.TopicWarnConfig;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.sohu.tv.mq.cloud.bo.TopicWarnConfig.OperandType.*;
import static com.sohu.tv.mq.cloud.bo.TopicWarnConfig.OperatorType.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicWarnServiceTest {

    @Autowired
    private TopicWarnService topicWarnService;

    @Autowired
    private TopicTrafficService topicTrafficService;

    public static final long tid = 313;

    @Test
    public void testWarnHour() {
        Result<List<TopicTraffic>> result = topicTrafficService.query(DateUtil.getBefore1Hour());
        if (result.isNotEmpty()) {
            topicWarnService.warnHour(result.getResult(), getHourTopicWarnConfig());
        }
    }

    @Test
    public void testWarnDay() {
        topicWarnService.warnDay(getDayTopicWarnConfig());
    }

    @Test
    public void testWarn5Minute() {
        topicWarnService.warn5Minute(get5MinuteTopicWarnConfig());
    }

    public Map<Long, List<TopicWarnConfig>> getHourTopicWarnConfig() {
        List<TopicWarnConfig> list = new ArrayList<>();

        // 1小时内发送量<=100
        TopicWarnConfig topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setTid(tid);
        topicWarnConfig.setOperandType(TRAFFIC_1_HOUR.getType());
        topicWarnConfig.setOperatorType(LESS_THAN_OR_EQUAL.getType());
        topicWarnConfig.setThreshold(100);
        topicWarnConfig.setWarnInterval(2);
        topicWarnConfig.setWarnTime("09:00-20:59");
        list.add(topicWarnConfig);

        // 时环比<=3%
        topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setTid(tid);
        topicWarnConfig.setOperandType(TRAFFIC_HOUR_TO_HOUR.getType());
        topicWarnConfig.setOperatorType(LESS_THAN_OR_EQUAL.getType());
        topicWarnConfig.setThreshold(-3);
        topicWarnConfig.setWarnInterval(2);
        topicWarnConfig.setWarnTime("09:00-20:59");
        list.add(topicWarnConfig);


        // time invalid
        topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setTid(tid);
        topicWarnConfig.setOperandType(TRAFFIC_1_HOUR.getType());
        topicWarnConfig.setOperatorType(LESS_THAN_OR_EQUAL.getType());
        topicWarnConfig.setThreshold(100);
        topicWarnConfig.setWarnInterval(2);
        topicWarnConfig.setWarnTime("22:00-08:59");
        list.add(topicWarnConfig);

        return topicWarnService.getTopicWarnConfigMap(() -> Result.getResult(list));
    }

    public Map<Long, List<TopicWarnConfig>> getDayTopicWarnConfig() {
        List<TopicWarnConfig> list = new ArrayList<>();
        // 日发送量<=100
        TopicWarnConfig topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setTid(tid);
        topicWarnConfig.setOperandType(TRAFFIC_1_DAY.getType());
        topicWarnConfig.setOperatorType(LESS_THAN.getType());
        topicWarnConfig.setThreshold(100);
        topicWarnConfig.setWarnInterval(2);
        topicWarnConfig.setWarnTime("09:00-20:59");
        list.add(topicWarnConfig);

        // 日环比<50%
        topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setTid(tid);
        topicWarnConfig.setOperandType(TRAFFIC_DAY_TO_DAY.getType());
        topicWarnConfig.setOperatorType(LESS_THAN.getType());
        topicWarnConfig.setThreshold(50);
        topicWarnConfig.setWarnInterval(2);
        topicWarnConfig.setWarnTime("09:00-20:59");
        list.add(topicWarnConfig);

        return topicWarnService.getTopicWarnConfigMap(() -> Result.getResult(list));
    }

    public Map<Long, List<TopicWarnConfig>> get5MinuteTopicWarnConfig() {
        List<TopicWarnConfig> list = new ArrayList<>();
        // 5分钟内发送量>100
        TopicWarnConfig topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setTid(tid);
        topicWarnConfig.setOperandType(TRAFFIC_5_MIN.getType());
        topicWarnConfig.setOperatorType(GREATER_THAN.getType());
        topicWarnConfig.setThreshold(100);
        topicWarnConfig.setWarnInterval(5);
        topicWarnConfig.setWarnTime("09:00-20:59");
        list.add(topicWarnConfig);

        // 5分钟内发送量<100
        topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setTid(tid);
        topicWarnConfig.setOperandType(TRAFFIC_5_MIN.getType());
        topicWarnConfig.setOperatorType(LESS_THAN.getType());
        topicWarnConfig.setThreshold(100);
        topicWarnConfig.setWarnInterval(5);
        topicWarnConfig.setWarnTime("09:00-20:59");
        list.add(topicWarnConfig);

        // 5分钟环比<0.1%
        topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setTid(tid);
        topicWarnConfig.setOperandType(TRAFFIC_MINUTE5_TO_MINUTE5.getType());
        topicWarnConfig.setOperatorType(LESS_THAN.getType());
        topicWarnConfig.setThreshold(0.1);
        list.add(topicWarnConfig);

        // 5分钟环比>=0.1%
        topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setTid(tid);
        topicWarnConfig.setOperandType(TRAFFIC_MINUTE5_TO_MINUTE5.getType());
        topicWarnConfig.setOperatorType(GREATER_THAN_OR_EQUAL.getType());
        topicWarnConfig.setThreshold(0.1);
        list.add(topicWarnConfig);
        return topicWarnService.getTopicWarnConfigMap(() -> Result.getResult(list));
    }

}