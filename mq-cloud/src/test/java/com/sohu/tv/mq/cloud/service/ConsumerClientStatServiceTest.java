package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ConsumerClientStat;
import com.sohu.tv.mq.cloud.util.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConsumerClientStatServiceTest {

    @Autowired
    private ConsumerClientStatService consumerClientStatService;

    @Test
    public void test(){
        String consumer = "broadcast-mqcloud-http-consumer";
        String client = "127.0.0.1";
        ConsumerClientStat consumerClientStat = new ConsumerClientStat(consumer, client);
        Result<Integer> result = consumerClientStatService.save(consumerClientStat);
        Assert.assertNotNull(result);
    }
}