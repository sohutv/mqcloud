package com.sohu.tv.mq.rocketmq;

import com.alibaba.fastjson.JSON;
import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.index.tv.mq.common.Result;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderTopicTest {

    private AtomicLong consumedMsgCount = new AtomicLong();

    @Test
    public void testOrderProduce() {
        RocketMQProducer producer = TestUtil.buildProducer("mqcloud-order-topic-producer", "mqcloud-order-topic");
        producer.setMessageQueueSelector((mqs, msg, arg) -> {
            int id = arg.hashCode();
            if (id < 0) {
                id = -id;
            }
            return mqs.get(id % mqs.size());
        });
        producer.start();

        List<Order> failedOrderList = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            Order order = new Order();
            order.orderId = UUID.randomUUID().toString();
            for (int status = 0; status <= 7; status++) {
                order.status = status;
                Result<SendResult> result = producer.publishOrder(order, order.orderId, order.orderId);
                if (!result.isSuccess()) {
                    failedOrderList.add(order);
                }
            }
            if (i % 100 == 0) {
                System.out.println("loop:" + i);
            }
        }
        System.out.println("failedOrderSize:" + failedOrderList.size());
        for (Order order : failedOrderList) {
            while (true) {
                Result<SendResult> result = producer.publishOrder(order, order.orderId, order.orderId);
                if (result.isSuccess()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    @Test
    public void testOrderConsume() {
        RocketMQConsumer consumer = TestUtil.buildConsumer("mqcloud-order-consumer", "mqcloud-order-topic");
        consumer.setConsumeOrderly(true);
        Map<String, Order> orderMap = new ConcurrentHashMap<>();
        consumer.setConsumerCallback((ConsumerCallback<String, MessageExt>) (orderString, k) -> {
            Order order = JSON.parseObject(orderString, Order.class);
            consumedMsgCount.incrementAndGet();
            Order prevOrder = orderMap.get(order.orderId);
            if (prevOrder != null) {
                Assert.assertTrue(prevOrder.status <= order.status);
            }
            orderMap.put(order.orderId, order);
        });
        consumer.start();

        while (true) {
            System.out.println(consumedMsgCount.get());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public static class Order {
        private String orderId;
        // 0:未支付 1:已支付 2:已发货 3:已收货 4:已评价 5:已完成 6:已取消 7:已退款
        private int status;

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
