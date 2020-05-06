package com.sohu.tv.mq.trace;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.remoting.RPCHook;

/**
 * 自己实现是为了使用MQCloud独立的集群
 * 
 * @author yongfeigao
 * @date 2019年2月20日
 */
public class SohuAsyncTraceDispatcher extends AsyncTraceDispatcher {

    public SohuAsyncTraceDispatcher(String traceTopicName) throws MQClientException, IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException {
        this(traceTopicName, null);
    }

    public SohuAsyncTraceDispatcher(String traceTopicName, RPCHook rpcHook) throws MQClientException,
            IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        super(null, null, traceTopicName, rpcHook);
    }

    @Override
    public void start(String nameSrvAddr, AccessChannel accessChannel) throws MQClientException {
        // 禁止AsyncTraceDispatcher启动自己的producer
        try {
            set("isStarted", new AtomicBoolean(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.start(nameSrvAddr, accessChannel);
    }

    public void start() throws MQClientException {
        // 启动dispatcher
        start(null, AccessChannel.LOCAL);
    }

    public int getMaxMsgSize()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Object value = get("maxMsgSize");
        if (value instanceof Integer) {
            return (Integer) value;
        }
        throw new NoSuchFieldException();
    }

    public void setTraceProducer(DefaultMQProducer producer)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        set("traceProducer", producer);
    }

    public void set(String fieldName, Object value)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        getField(fieldName).set(this, value);
    }

    public Object get(String fieldName)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return getField(fieldName).get(this);
    }

    private Field getField(String fieldName) throws NoSuchFieldException, SecurityException {
        Field field = AsyncTraceDispatcher.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
