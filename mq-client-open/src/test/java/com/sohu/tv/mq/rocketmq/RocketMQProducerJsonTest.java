package com.sohu.tv.mq.rocketmq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.sohu.index.tv.mq.common.Result;

public class RocketMQProducerJsonTest {

    private RocketMQProducer producer;

    @Before
    public void init() {
        producer = TestUtil.buildProducer("mqcloud-test-topic-producer", "mqcloud-test-topic");
        producer.start();
    }

    @Test
    public void produce() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", "b");
        map.put("c", "d");
        map.put("o", "c");
        String str = JSON.toJSONString(map);
        Result<SendResult> sendResult = producer.publish(str, "abc");
        System.out.println(sendResult);
        Assert.assertTrue(sendResult.isSuccess());
    }
    
    @Test
    public void produceMulti() throws Exception {
        for(int i = 0; i < 10000; ++i) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("a", "a"+i);
            map.put("c", "d"+i);
            map.put("o", "c"+i);
            String str = JSON.toJSONString(map);
            Result<SendResult> sendResult = producer.publish(str, "data"+i);
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
        long vid = 123L;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("v", vid);
        Result<SendResult> sendResult = producer.publishOrder(map, String.valueOf(vid), vid);
        Assert.assertNotNull(sendResult);
    }
    
    @After
    public void clean() {
        producer.shutdown();
    }
}
