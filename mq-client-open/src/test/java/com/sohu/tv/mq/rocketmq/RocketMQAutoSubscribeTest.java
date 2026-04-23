package com.sohu.tv.mq.rocketmq;

import com.sohu.index.tv.mq.common.MQMessage;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RocketMQAutoSubscribeTest {

    public static final String TOPIC = "mqcloud-json-test-topic";
    public static final String SEPARATOR = "_";

    private RocketMQProducer producer;

    @Before
    public void init() {
        producer = TestUtil.buildProducer(TOPIC + "-producer", TOPIC);
        producer.start();
    }

    @Test
    public void testAutoSubscribe() throws InterruptedException {
        int begin = 0;
        int end = 10;
        String tagName = "tag";
        String consumerSuffix = "consumer";
        ConcurrentMap<String, List<String>> consumerTagMap = new ConcurrentHashMap<>();
        for (int i = begin; i < end; ++i) {
            producer.send(MQMessage.build(addSuffix("msg", i)).setTags(addSuffix(tagName, i)));
            RocketMQConsumer consumer = TestUtil.buildConsumer(addSuffix(consumerSuffix, i), TOPIC);
            consumer.subscribeTag(addSuffix(tagName, i));
            if (i == begin) {
                consumer.subscribeTag(addSuffix(tagName, i + 1));
            }
            consumer.setConsumerCallback((t, k) -> {
                MessageExt msgExt = (MessageExt) k;
                consumerTagMap.computeIfAbsent(consumer.getGroup(), c -> new ArrayList()).add(msgExt.getTags());
            });
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.start();
        }
        Thread.sleep(3 * 1000);
        for (int i = begin; i < end; ++i) {
            String consumerName = addSuffix(consumerSuffix, i);
            List<String> receivedTags = consumerTagMap.get(consumerName);
            if (i == begin) {
                assert receivedTags.size() == 2;
                assert receivedTags.contains(addSuffix(tagName, i));
                assert receivedTags.contains(addSuffix(tagName, i + 1));
            } else {
                assert receivedTags.size() == 1;
                assert receivedTags.contains(addSuffix(tagName, i));
            }
        }
        Thread.sleep(10 * 1000);
    }

    private String addSuffix(String str, Object suffix) {
        return str + SEPARATOR + suffix;
    }
}
