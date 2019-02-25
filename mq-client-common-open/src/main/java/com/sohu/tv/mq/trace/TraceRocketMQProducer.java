package com.sohu.tv.mq.trace;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

import com.sohu.tv.mq.common.AbstractConfig;
/**
 * 专门发送trace数据的producer
 * 
 * @author yongfeigao
 * @date 2019年2月20日
 */
public class TraceRocketMQProducer extends AbstractConfig {
    // rocketmq 实际生产者
    private final DefaultMQProducer producer;
    
    private volatile boolean started = false;
    
    public TraceRocketMQProducer(String group, String topic) {
        super(group, topic);
        producer = new DefaultMQProducer(group);
        setSampleEnabled(false);
    }

    /**
     * 启动
     */
    public void start() {
        if(started) {
            logger.info("started! ignore.");
            return;
        }
        started = true;
        try {
            // 初始化配置
            initConfig(producer);
            producer.start();
            logger.info("trace topic:{} group:{} start", topic, group);
        } catch (MQClientException e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isSampleEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    public DefaultMQProducer getProducer() {
        return producer;
    }

    @Override
    protected int role() {
        return PRODUCER;
    }
}
