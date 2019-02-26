package com.sohu.tv.mq.rocketmq;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.rocketmq.common.message.MessageExt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.ConsumerCallback;

public class RocketMQConsumerStringSerializerTest {

    private AtomicLong counter = new AtomicLong();

    private RocketMQConsumer consumer;

    @Before
    public void init() {
        consumer = new RocketMQConsumer("mqcloud-string-serializer-topic-consumer", "mqcloud-string-serializer-topic");
        consumer.setMqCloudDomain("mqcloud.com:80");
        consumer.setTraceEnabled(true);
    }

    @Test
    public void test() throws InterruptedException {
        consumer.setConsumerCallback(new ConsumerCallback<String, MessageExt>() {
            public void call(String t, MessageExt k) {
                if (counter.incrementAndGet() % 10 == 0) {
                    System.out.println(t);
                }
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
