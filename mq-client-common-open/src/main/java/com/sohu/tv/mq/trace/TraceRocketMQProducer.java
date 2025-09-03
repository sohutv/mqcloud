package com.sohu.tv.mq.trace;

import com.sohu.tv.mq.common.AbstractConfig;
import com.sohu.tv.mq.common.MQCloudClientException;
import com.sohu.tv.mq.route.AffinityMQStrategy;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.ServiceState;
import org.apache.rocketmq.remoting.RPCHook;

import java.lang.reflect.Field;

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
        } catch (MQCloudClientException e) {
            logger.error("init config error, group:{}", group, e);
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

    @Override
    protected void initAffinity() {
        super.initAffinity();
        if (isAffinityEnabled()) {
            try {
                Field field = DefaultMQProducerImpl.class.getDeclaredField("mqFaultStrategy");
                field.setAccessible(true);
                field.set(producer.getDefaultMQProducerImpl(), new AffinityMQStrategy(getAffinityBrokerSuffix(),
                        isAffinityIfBrokerNotSet()));
                logger.info("{} initAffinity:{}", group, getAffinityBrokerSuffix());
            } catch (Exception e) {
                logger.error("initAffinity error", e);
            }
        }
    }

    @Override
    public void setAclRPCHook(RPCHook rpcHook) {
        try {
            Field rpcHookField = DefaultMQProducerImpl.class.getDeclaredField("rpcHook");
            rpcHookField.setAccessible(true);
            rpcHookField.set(producer.getDefaultMQProducerImpl(), rpcHook);
        } catch (Exception e) {
            throw new RuntimeException("setAcl error, group:" + getGroup());
        }
    }

    @Override
    protected Object getMQClient() {
        return producer;
    }

    @Override
    public ServiceState getServiceState() {
        return producer.getDefaultMQProducerImpl().getServiceState();
    }
}
