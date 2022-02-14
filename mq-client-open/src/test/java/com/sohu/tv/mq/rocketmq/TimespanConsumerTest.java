package com.sohu.tv.mq.rocketmq;

import java.util.Map;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.tv.mq.util.CommonUtil;

public class TimespanConsumerTest {

    private RocketMQConsumer consumer;

    @Before
    public void init() {
        consumer = TestUtil.buildConsumer("basic-apitest-topic-consumer", "basic-apitest-topic");
        consumer.setConsumerCallback(new ConsumerCallback<Map<String, Object>, MessageExt>() {
            public void call(Map<String, Object> t, MessageExt k) {
                if (CommonUtil.isDeadTopic(k.getTopic())) {
                    System.out.println(t);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        consumer.start();
    }

    @Test
    public void test() throws InterruptedException {
        long start = 1637305140000L;
        long end = 1637305800000L;
        String topic = MixAll.getDLQTopic("basic-apitest-topic-consumer");
        new TimespanConsumer(consumer, topic, start, end).start();
    }

}
