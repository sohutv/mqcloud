package com.sohu.tv.mq.rocketmq;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PublishAsyncCommandTest {
    private RocketMQProducer producer;

    private SendCallback sendCallback;

    @Before
    public void init() {
        sendCallback = new SendCallback() {
            public void onSuccess(SendResult sendResult) {
                // 成功回调
            }

            public void onException(Throwable e) {
                // 失败回掉
            }
        };
        producer = TestUtil.buildProducer("core-vrs-topic-producer", "core-vrs-topic");
        producer.start();
    }

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < 1000; ++i) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("aid", "123456" + i);
            map.put("vid", "123457" + i);
            new PublishAsyncCommand(producer, map, sendCallback).execute();
            Thread.sleep(500);
        }
    }

    @After
    public void clean() {
        producer.shutdown();
    }
}
