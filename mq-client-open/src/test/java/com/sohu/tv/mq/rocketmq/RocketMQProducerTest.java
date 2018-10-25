package com.sohu.tv.mq.rocketmq;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.client.producer.SendResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.util.SerializerTest;

public class RocketMQProducerTest {

    private RocketMQProducer producer;

    @Before
    public void init() {
        producer = TestUtil.buildProducer("basic-apitest-topic-producer", "basic-apitest-topic");
        producer.start();
    }

    @Test
    public void produce() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", "b");
        map.put("c", "d");
        map.put("o", SerializerTest.generateTestObject());
        Result<SendResult> sendResult = producer.publish(map);
        Assert.assertTrue(sendResult.isSuccess());
    }

    @Test
    public void produce100() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("c", "d");
        map.put("o", SerializerTest.generateTestObject());
        for (int i = 0; i < 1000; i++) {
            map.put("a", i);
            Result<SendResult> sendResult = producer.publish(map);
            Assert.assertTrue(sendResult.isSuccess());
            Thread.sleep(1000);
        }
    }

    @Test
    public void produceByte() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", "b");
        map.put("c", "d");
        map.put("o", SerializerTest.generateTestObject());
        Result<SendResult> sendResult = producer.publish(map.toString().getBytes());
        Assert.assertTrue(sendResult.isSuccess());
    }

    @After
    public void clean() {
        producer.shutdown();
    }
}
