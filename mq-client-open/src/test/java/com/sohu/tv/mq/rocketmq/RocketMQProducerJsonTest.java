package com.sohu.tv.mq.rocketmq;

import java.util.List;

import com.sohu.tv.mq.util.JSONUtil;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.Result;

public class RocketMQProducerJsonTest {

    private RocketMQProducer producer;

    @Before
    public void init() {
        producer = TestUtil.buildProducer("mqcloud-json-test-topic-producer", "mqcloud-json-test-topic");
        producer.setFetchTopicRouteInfoWhenStart(true);
        producer.start();
    }

    @Test
    public void produce() {
        Video video = new Video(1, "搜狐tv");
        String str = JSONUtil.toJSONString(video);
        Result<SendResult> sendResult = producer.publish(str, String.valueOf(1));
        System.out.println(sendResult);
        Assert.assertTrue(sendResult.isSuccess());
    }
    
    @Test
    public void produceMulti() throws Exception {
        for(int i = 0; i < 10000; ++i) {
            Video video = new Video(i, "搜狐tv"+i);
            String str = JSONUtil.toJSONString(video);
            Result<SendResult> sendResult = producer.publish(str, String.valueOf(i));
            System.out.println(sendResult);
            Assert.assertTrue(sendResult.isSuccess());
            Thread.sleep(1000);
        }
    }
    
    /**
     * 相同的id发送到同一个队列
     * hash方法：id % 队列数
     */
    class IDHashMessageQueueSelector implements MessageQueueSelector {
        public MessageQueue select(List<MessageQueue> mqs, Message msg, Object idObject) {
            long id = (Long) idObject;
            int size = mqs.size();
            int index = (int) (id % size);
            return mqs.get(index);
        }
    }
    
    @Test
    public void produceOrder() {
        producer.setMessageQueueSelector(new IDHashMessageQueueSelector());
        Video video = new Video(123, "搜狐tv");
        String str = JSONUtil.toJSONString(video);
        Result<SendResult> sendResult = producer.publishOrder(str, String.valueOf(video.getId()), video.getId());
        Assert.assertNotNull(sendResult);
    }
    
    @After
    public void clean() {
        producer.shutdown();
    }
    
    public static class Video {
        private int id;
        private String name;
        public Video(int id, String name) {
            this.id = id;
            this.name = name;
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }
}
