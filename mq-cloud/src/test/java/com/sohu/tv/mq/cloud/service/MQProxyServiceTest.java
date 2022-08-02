package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.QueueOffset;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.service.MQProxyService.ConsumerConfigParam;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author: yongfeigao
 * @date: 2022/6/7 10:19
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MQProxyServiceTest {

    @Autowired
    private MQProxyService mqProxyService;

    @Test
    public void testQueueOffset() {
        String consumer = "mqcloud-json-test-bd-consumer";
        Result<List<QueueOffset>> result = mqProxyService.clusteringQueueOffset(consumer);
        Assert.assertNotNull(result);
    }

    @Test
    public void testConsumerConfig() {
        ConsumerConfigParam configParam = new ConsumerConfigParam();
        configParam.setConsumer("http-clustering-consumer");
        configParam.setRateLimitEnabled(1);
        configParam.setLimitRate(32D);
        Result<?> result = mqProxyService.consumerConfig(getUserInfo(), configParam);
        Assert.assertEquals(true, result.isOK());
    }

    private UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        Result<User> userResult = new Result<>();
        User user = new User();
        user.setEmail("admin@admin.com");
        userResult.setResult(user);
        userInfo.setUserResult(userResult);
        return userInfo;
    }
}