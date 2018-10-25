package com.sohu.tv.mq.rocketmq;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.rocketmq.common.message.MessageExt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.ConsumerCallback;

public class RocketMQConsumerTest {

    private AtomicLong counter = new AtomicLong();

    private RocketMQConsumer consumer;

    @Before
    public void init() {
        consumer = TestUtil.buildConsumer("basic-apitest-topic-consumer", "basic-apitest-topic");
    }

    @Test
    public void test() throws InterruptedException {
        consumer.setConsumerCallback(new ConsumerCallback<Map<String, Object>, MessageExt>() {
            public void call(Map<String, Object> t, MessageExt k) {
                if (counter.incrementAndGet() % 10 == 0) {
                    System.out.println(t);
                }
            }
        });
        consumer.start();
        while (true) {
            System.out.println(counter.get());
            Thread.sleep(1000);
        }
    }

    @Test
    public void testConsumeByte() throws InterruptedException {
        consumer.setConsumerCallback(new ConsumerCallback<byte[], MessageExt>() {
            public void call(byte[] t, MessageExt k) throws Exception {
                System.out.println(new String(t));
            }
        });
        consumer.start();
        while (true) {
            System.out.println(counter.get());
            Thread.sleep(1000);
        }
    }

    @After
    public void clean() {
        consumer.shutdown();
    }
}
