package com.sohu.tv.mq.rocketmq;

import java.util.HashMap;
import java.util.Map;

import com.sohu.tv.mq.util.JSONUtil;
import org.apache.rocketmq.client.producer.SendResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.serializable.StringSerializer;

public class RocketMQProducerStringSerializerTest {

    private RocketMQProducer producer;

    @Before
    public void init() {
        producer = TestUtil.buildProducer("mqcloud-string-serializer-topic-producer", "mqcloud-string-serializer-topic");
        producer.setMessageSerializer(new StringSerializer<>());
        producer.start();
    }

    @Test
    public void produce100() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < 100; i++) {
            map.put("a", i);
            Result<SendResult> sendResult = producer.publish(JSONUtil.toJSONString(map), "a" + i);
            Assert.assertTrue(sendResult.isSuccess());
            Thread.sleep(3000);
        }
    }

    @After
    public void clean() {
        producer.shutdown();
    }
}
