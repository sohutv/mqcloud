package com.sohu.tv.mq.rocketmq;

import com.sohu.index.tv.mq.common.ConsumerCallback;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static com.sohu.tv.mq.rocketmq.LmqProducerTest.LMQ_TOPIC2;
import static com.sohu.tv.mq.rocketmq.LmqProducerTest.TOPIC;

public class RocketMQLmqConsumerBroadcastTest {
    private AtomicLong counter = new AtomicLong();

    private RocketMQLmqConsumer consumer;

    @Before
    public void init() {
        consumer = TestUtil.buildLMQConsumer("broadcastConsumer", TOPIC, LMQ_TOPIC2);
    }

    @Test
    public void test() throws InterruptedException {
        consumer.setConsumerCallback((ConsumerCallback<String, MessageExt>) (t, k) -> {
            counter.incrementAndGet();
        });
        consumer.setMessageModelToBroadcasting();
        consumer.start();
        while (true) {
            System.out.println(counter.get());
            Thread.sleep(3000);
        }
    }

    @After
    public void clean() {
        consumer.shutdown();
    }
}
