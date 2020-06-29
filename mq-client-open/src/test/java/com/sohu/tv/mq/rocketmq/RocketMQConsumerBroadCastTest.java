package com.sohu.tv.mq.rocketmq;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.rocketmq.common.message.MessageExt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.ConsumerCallback;

public class RocketMQConsumerBroadCastTest {

    private AtomicLong counter = new AtomicLong();

    private RocketMQConsumer consumer;

    @Before
    public void init() {
        consumer = TestUtil.buildConsumer("basic-apitest-broadcast-consumer", "basic-apitest-topic");
    }
    
    @Test
    public void test() throws InterruptedException {
        consumer.setConsumerCallback(new ConsumerCallback<Map<String, Object>, MessageExt>() {
            public void call(Map<String, Object> t, MessageExt k) throws InterruptedException {
                counter.incrementAndGet();
                Thread.sleep(100);
            }
        });
        consumer.start();
        while (true) {
            System.out.println(counter.get());
            Thread.sleep(10000);
        }
    }

    @After
    public void clean() {
        consumer.shutdown();
    }
}
