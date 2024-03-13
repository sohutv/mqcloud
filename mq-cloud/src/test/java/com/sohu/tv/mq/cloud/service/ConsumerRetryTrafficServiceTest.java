package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.TopicConsumer;
import com.sohu.tv.mq.cloud.bo.TopicHourTraffic;
import com.sohu.tv.mq.cloud.bo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConsumerRetryTrafficServiceTest {

    @Autowired
    private ConsumerRetryTrafficService consumerRetryTrafficService;
    
    @Test
    public void test() {
        consumerRetryTrafficService.collectHourTraffic();
    }

    @Test
    public void testAlert() {
        TopicHourTraffic topicTraffic = new TopicHourTraffic();
        TopicConsumer topicConsumer = new TopicConsumer();
        topicConsumer.setTopic("abc-topic");
        topicConsumer.setConsumer("abc-consumer");
        List<User> userList = new ArrayList<>();
        User user = new User();
        user.setEmail("admin@admin.com");
        userList.add(user);
        consumerRetryTrafficService.alert(topicTraffic, topicConsumer, userList);
    }

}
