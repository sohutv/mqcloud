package com.sohu.tv.mq.rocketmq;

import com.sohu.index.tv.mq.common.PullResponse;
import com.sohu.index.tv.mq.common.PullResponse.Status;
import com.sohu.tv.mq.common.AbstractConfig;
import com.sohu.tv.mq.rocketmq.limiter.NoneBlockingRateLimiter;
import com.sohu.tv.mq.util.JSONUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPullConsumerImpl;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.client.trace.hook.ConsumeMessageTraceHookImpl;
import org.apache.rocketmq.common.ServiceState;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: yongfeigao
 * @date: 2022/5/30 11:09
 */
public class RocketMQPullConsumer extends AbstractConfig {
    // 消费者
    private DefaultMQPullConsumer consumer;

    // 每批消息消最大拉取量
    private int maxPullSize = 32;

    // 每批消息消费超时毫秒
    private long consumeTimeoutInMillis = 5 * 60 * 1000;

    // 是否暂停消费
    private boolean pause;

    private NoneBlockingRateLimiter rateLimiter;

    public RocketMQPullConsumer() {
    }

    public RocketMQPullConsumer(String group, String topic) {
        construct(group, topic);
    }

    /**
     * 初始化
     */
    public RocketMQPullConsumer construct(String group, String topic) {
        setTopic(topic);
        setGroup(group);
        consumer = new DefaultMQPullConsumer(group);
        rateLimiter = new NoneBlockingRateLimiter(1024);
        return this;
    }

    public void start() {
        try {
            Set<String> registerTopics = new HashSet<>();
            registerTopics.add(topic);
            consumer.setRegisterTopics(registerTopics);
            // 初始化配置
            initConfig(consumer);
            if (getClusterInfoDTO().isBroadcast()) {
                consumer.setMessageModel(MessageModel.BROADCASTING);
            }
            // 消费者启动
            consumer.start();
            logger.info("topic:{} group:{} start", topic, group);
        } catch (MQClientException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 拉取消息
     *
     * @param mq
     * @param offset
     * @return PullResponse
     * @throws MQClientException
     * @throws RemotingException
     * @throws MQBrokerException
     * @throws InterruptedException
     */
    public PullResponse pull(MessageQueue mq, long offset)
            throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        if (isPause()) {
            return PullResponse.build(Status.PAUSED);
        }
        // 执行限速
        if (!rateLimiter.acquire()) {
            return PullResponse.build(Status.RATE_LIMITED);
        }
        PullResult rullResult = consumer.pull(mq, "*", offset, maxPullSize);
        // 执行限速统计
        if (rullResult.getMsgFoundList() != null) {
            int size = rullResult.getMsgFoundList().size();
            if (size > 0) {
                rateLimiter.acquire(size);
            }
        }
        return PullResponse.build(rullResult);
    }

    public boolean isRunning() {
        return ServiceState.RUNNING == consumer.getDefaultMQPullConsumerImpl().getServiceState();
    }

    public void shutdown() {
        consumer.shutdown();
    }

    /**
     * 反序列化为String
     *
     * @param me
     * @return
     * @throws Exception
     */
    public String deserialize(MessageExt me) throws Exception {
        Object msgObj = getMessageSerializer().deserialize(me.getBody());
        if (msgObj instanceof String) {
            return (String) msgObj;
        }
        return JSONUtil.toJSONString(msgObj);
    }

    public DefaultMQPullConsumer getConsumer() {
        return consumer;
    }

    public int getMaxPullSize() {
        return maxPullSize;
    }

    public void setMaxPullSize(int maxPullSize) {
        this.maxPullSize = maxPullSize;
    }

    public long getConsumeTimeoutInMillis() {
        return consumeTimeoutInMillis;
    }

    public void setConsumeTimeoutInMillis(long consumeTimeoutInMillis) {
        this.consumeTimeoutInMillis = consumeTimeoutInMillis;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    @Override
    public int role() {
        return CONSUMER;
    }

    public NoneBlockingRateLimiter getRateLimiter() {
        return rateLimiter;
    }

    @Override
    public void setAclRPCHook(RPCHook rpcHook) {
        try {
            Field rpcHookField = DefaultMQPullConsumerImpl.class.getDeclaredField("rpcHook");
            rpcHookField.setAccessible(true);
            rpcHookField.set(consumer.getDefaultMQPullConsumerImpl(), rpcHook);
        } catch (Exception e) {
            throw new RuntimeException("setAcl error, group:" + getGroup());
        }
    }

    @Override
    protected Object getMQClient() {
        return consumer;
    }

    @Override
    protected void registerTraceDispatcher(AsyncTraceDispatcher traceDispatcher) {
        consumer.getDefaultMQPullConsumerImpl().registerConsumeMessageHook(
                new ConsumeMessageTraceHookImpl(traceDispatcher));
    }
}
