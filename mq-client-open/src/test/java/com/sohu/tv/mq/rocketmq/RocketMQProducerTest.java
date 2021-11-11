package com.sohu.tv.mq.rocketmq;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.client.producer.SendResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.index.tv.mq.common.Result;

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
        map.put("o", "c");
        Result<SendResult> sendResult = producer.publish(map);
        Assert.assertTrue(sendResult.isSuccess());
    }

    @Test
    public void produce100() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("c", "d");
        map.put("o", "c");
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
        map.put("o", "c");
        Result<SendResult> sendResult = producer.publish(map.toString().getBytes());
        Assert.assertTrue(sendResult.isSuccess());
    }
    
    @Test
    public void testRetry() throws InterruptedException {
        producer.setResendResultConsumer(result -> {
            System.out.println("---msgId:"+result.getResult().getMsgId()+",offsetMsgId:"+result.getResult().getOffsetMsgId());
        });
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", "b");
        map.put("c", "d");
        map.put("o", "c");
        Result<SendResult> result = producer.send(MQMessage.build(map).setKeys("abc").setExceptionForTest(true));
        if (!result.isSuccess && !result.isRetrying()) {
            System.out.println("发送失败");
        }
        System.out.println("=="+result);
        Thread.sleep(3 * 1000);
    }
    
    @Test
    public void testRetryReject() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", "b");
        map.put("c", "d");
        map.put("o", "c");
        for(int i = 0; i < 200; ++i) {
            Result<SendResult> result = producer.send(MQMessage.build(map).setKeys("abc"));
            System.out.println("=="+i+"="+result);
        }
        Thread.sleep(3 * 1000);
    }
    
    @Test
    public void testIdempotentMessage() throws InterruptedException {
        producer.setResendResultConsumer(result -> {
            System.out.println("---msgId:"+result.getResult().getMsgId()+",offsetMsgId:"+result.getResult().getOffsetMsgId());
        });
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", "b");
        map.put("c", "d");
        map.put("o", "c");
        MQMessage<String> message = MQMessage.build("message").setKeys("k1").setIdempotentID("1102123");
        Result<SendResult> result = producer.send(message);
        if (!result.isSuccess && !result.isRetrying()) {
            System.out.println("发送失败");
        }
        System.out.println("=="+result);
        Thread.sleep(3 * 1000);
    }

    @After
    public void clean() {
        producer.shutdown();
    }
}
