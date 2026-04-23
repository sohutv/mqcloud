package com.sohu.tv.mq.rocketmq;

import com.sohu.index.tv.mq.common.ConsumerCallback;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static com.sohu.tv.mq.rocketmq.LmqProducerTest.LMQ_TOPIC1;
import static com.sohu.tv.mq.rocketmq.LmqProducerTest.TOPIC;

public class RocketMQLmqConsumerTest {

    private AtomicLong counter = new AtomicLong();

    private RocketMQLmqConsumer consumer;

    @Before
    public void init() {
        consumer = TestUtil.buildLMQConsumer("clusterConsumer", TOPIC, LMQ_TOPIC1);
    }

    @Test
    public void test() throws InterruptedException {
        consumer.setConsumerCallback((ConsumerCallback<String, MessageExt>) (t, k) -> {
            counter.incrementAndGet();
        });
        consumer.start();
        while (true) {
            System.out.println(counter.get());
            Thread.sleep(3000);
        }
    }

    @Test
    public void testRetry() throws InterruptedException {
        AtomicLong curTime = new AtomicLong(System.currentTimeMillis());
        consumer.setConsumerCallback((ConsumerCallback<String, MessageExt>) (t, k) -> {
            if (t.equals("msg0")) {
                counter.incrementAndGet();
                System.out.println("consume msg:" + t + " times:" + counter.get() + " after:" + (System.currentTimeMillis() - curTime.get()) + "ms");
                curTime.set(System.currentTimeMillis());
                throw new RuntimeException("consume failed");
            }
        });
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
