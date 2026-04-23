package com.sohu.tv.mq.rocketmq;

import com.sohu.index.tv.mq.common.PullResponse;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static com.sohu.tv.mq.rocketmq.LmqProducerTest.LMQ_TOPIC1;
import static com.sohu.tv.mq.rocketmq.LmqProducerTest.TOPIC;

public class RocketMQLmqPullConsumerTest {
    private AtomicLong counter = new AtomicLong();

    private RocketMQLmqPullConsumer consumer;

    @Before
    public void init() {
        consumer = TestUtil.buildLMQPullConsumer("pullConsumer", TOPIC, LMQ_TOPIC1);
        consumer.start();
    }

    @Test
    public void test() throws Exception {
        while (true) {
            Set<MessageQueue> messageQueues = consumer.fetchSubscribeMessageQueues();
            if (messageQueues == null || messageQueues.isEmpty()) {
                Thread.sleep(1000);
                continue;
            }
            for (MessageQueue messageQueue : messageQueues) {
                long offset = consumer.getConsumer().fetchConsumeOffset(messageQueue, false);
                if (offset < 0) {
                    offset = 0;
                }
                PullResponse pullResponse = consumer.pull(messageQueue, offset);
                switch (pullResponse.getStatus()) {
                    case FOUND:
                        List<MessageExt> msgs = pullResponse.getMsgList();
                        if (msgs == null || msgs.isEmpty()) {
                            continue;
                        }
                        counter.addAndGet(msgs.size());
                        System.out.println("pull " + msgs.size() + " messages, total: " + counter.get());
                        consumer.getConsumer().updateConsumeOffset(messageQueue, pullResponse.getNextOffset());
                        break;
                    case NO_NEW_MSG:
                    case OFFSET_ILLEGAL:
                    case NO_MATCHED_MSG:
                        consumer.getConsumer().updateConsumeOffset(messageQueue, pullResponse.getNextOffset());
                        Thread.sleep(1000);
                }
            }
        }
    }
}