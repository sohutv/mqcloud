package com.sohu.tv.mq.cloud.mq;

import java.lang.reflect.Field;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExtImpl;

/**
 * sohu实现，为了添加扩展某些方法
 * 
 * @author yongfeigao
 * @date 2018年10月16日
 */
public class SohuMQAdmin extends DefaultMQAdminExt {

    public SohuMQAdmin() {
        super();
    }

    public SohuMQAdmin(long timeoutMillis) {
        super(timeoutMillis);
    }

    public SohuMQAdmin(RPCHook rpcHook, long timeoutMillis) {
        super(rpcHook, timeoutMillis);
    }

    public SohuMQAdmin(RPCHook rpcHook) {
        super(rpcHook);
    }

    public SohuMQAdmin(String adminExtGroup, long timeoutMillis) {
        super(adminExtGroup, timeoutMillis);
    }

    public SohuMQAdmin(String adminExtGroup) {
        super(adminExtGroup);
    }

    /**
     * 获取系统内置topic
     * 
     * @param timeoutMillis
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws RemotingException
     * @throws MQClientException
     * @throws InterruptedException
     */
    public TopicList getSystemTopicList(long timeoutMillis)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
            RemotingException, MQClientException, InterruptedException {
        return getMQClientInstance().getMQClientAPIImpl().getSystemTopicList(timeoutMillis);
    }

    /**
     * 发送消息
     * 
     * @param msg
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws MQClientException
     * @throws RemotingException
     * @throws MQBrokerException
     * @throws InterruptedException
     */
    public SendResult sendMessage(Message msg) throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, MQClientException, RemotingException, MQBrokerException, InterruptedException {
        return getMQClientInstance().getDefaultMQProducer().send(msg);
    }

    /**
     * 获取 @MQClientInstance
     * 
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public MQClientInstance getMQClientInstance()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        // 反射获取defaultMQAdminExtImpl实例
        Field defaultMQAdminExtImplField = DefaultMQAdminExt.class.getDeclaredField("defaultMQAdminExtImpl");
        defaultMQAdminExtImplField.setAccessible(true);
        DefaultMQAdminExtImpl defaultMQAdminExtImpl = (DefaultMQAdminExtImpl) defaultMQAdminExtImplField.get(this);
        // 反射获取mqClientInstance实例
        Field field = DefaultMQAdminExtImpl.class.getDeclaredField("mqClientInstance");
        field.setAccessible(true);
        MQClientInstance mqClientInstance = (MQClientInstance) field.get(defaultMQAdminExtImpl);
        return mqClientInstance;
    }
}
