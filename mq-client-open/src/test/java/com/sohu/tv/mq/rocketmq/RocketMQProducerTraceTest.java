package com.sohu.tv.mq.rocketmq;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.client.producer.SendResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.Result;

public class RocketMQProducerTraceTest {
    private RocketMQProducer producer;
    
    @Before
    public void init() {
        producer = TestUtil.buildProducer("basic-apitest-topic-producer", "basic-apitest-topic");
        producer.start();
    }

    @Test
    public void produce100() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < 10; i++) {
            map.put("data", i);
            Result<SendResult> sendResult = producer.publish(map, "data"+i);
            Assert.assertTrue(sendResult.isSuccess());
            Thread.sleep(1000);
        }
        Thread.sleep(60 * 1000);
    }
    
    @After
    public void clean() {
        producer.shutdown();
    }
}
