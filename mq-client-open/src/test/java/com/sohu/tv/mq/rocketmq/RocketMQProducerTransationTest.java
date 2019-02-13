package com.sohu.tv.mq.rocketmq;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.sohu.index.tv.mq.common.Result;

public class RocketMQProducerTransationTest {

    private RocketMQProducer producer;

    @Before
    public void init() {
        TransactionListener transactionListener = new TransactionListener() {
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                int id = (Integer) arg;
                if(id == 1) {
                    System.out.println("executeLocalTransaction id:" + id + " UNKNOW");
                    return LocalTransactionState.UNKNOW;
                }
                if(id == 2) {
                    System.out.println("executeLocalTransaction id:" + id + " UNKNOW");
                    return LocalTransactionState.UNKNOW;
                }
                if(id == 3) {
                    System.out.println("executeLocalTransaction id:" + id + " ROLLBACK_MESSAGE");
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                String key = msg.getKeys();
                int id = Integer.valueOf(key);
                System.out.println("checkLocalTransaction id:" + id);
                if(id == 1) {
                    System.out.println("UNKNOW");
                    return LocalTransactionState.UNKNOW;
                }
                if(id == 2) {
                    System.out.println("ROLLBACK_MESSAGE");
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        };
        producer = TestUtil.buildProducer("mqcloud-test-topic-trans-producer", "mqcloud-test-topic", transactionListener);
        producer.start();
    }

    @Test
    public void produce() throws InterruptedException {
        for(int i = 1; i < 10; ++i) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", i);
            map.put("msg", "msg" +i);
            Result<SendResult> sendResult = producer.publishTransaction(JSON.toJSONString(map), String.valueOf(i), i);
            System.out.println(sendResult);
            Assert.assertTrue(sendResult.isSuccess());
        }
        Thread.sleep(20 * 60 * 1000);
    }
    
    @After
    public void clean() {
        producer.shutdown();
    }
}
