package com.sohu.tv.mq.rocketmq;

import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.index.tv.mq.common.Result;
import org.apache.rocketmq.client.producer.SendResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LmqProducerTest {

    public static final String TOPIC = "lmq-test-topic";

    public static final String LMQ_TOPIC1 = "lmq1";

    public static final String LMQ_TOPIC2 = "lmq2";

    private RocketMQProducer producer;

    @Before
    public void init() {
        producer = TestUtil.buildProducer("lmq-test-topic-producer", TOPIC);
        producer.start();
    }

    @Test
    public void produce() {
        for (int i = 0; i < 10; ++i) {
            try {
                Result<SendResult> sendResult = producer.send(MQMessage.build("msg"+i)
                        .addLiteTopic(LMQ_TOPIC1).addLiteTopic(LMQ_TOPIC2));
                Assert.assertTrue(sendResult.isSuccess());
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void produceOrderly() {
        for (int i = 0; i < 10; ++i) {
            try {
                String msg = "msg"+i;
                Result<SendResult> sendResult = producer.send(MQMessage.build(msg)
                        .addLiteTopic(LMQ_TOPIC1)
                        .setOrderArg("msg"));
                Assert.assertTrue(sendResult.isSuccess());
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void clean() {
        producer.shutdown();
    }
}
