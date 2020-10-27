package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author yongweizhao
 * @create 2020/8/5 10:36
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicTrafficStatServiceTest {
    @Autowired
    private TopicTrafficStatService topicTrafficStatService;

    @Test
    public void test() {
        topicTrafficStatService.trafficStatAll();
    }

}
