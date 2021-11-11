package com.sohu.tv.mq.rocketmq;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.client.trace.hook.SendMessageTraceHookImpl;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.common.AbstractConfig;
import com.sohu.tv.mq.common.SohuSendMessageHook;
import com.sohu.tv.mq.metric.MQMetricsExporter;
import com.sohu.tv.mq.stats.StatsHelper;

/**
 * rocketmq producer 封装
 * 
 * @date 2018年1月17日
 * @author copy from indexmq
 */
@SuppressWarnings("deprecation")
public class RocketMQProducer extends AbstractConfig {
    // rocketmq 实际生产者
    private final DefaultMQProducer producer;
    
    // 统计助手
    private StatsHelper statsHelper;
    
    // 发送顺序消息使用
    private MessageQueueSelector messageQueueSelector;
    
    // 默认重试次数
    private int defaultRetryTimes = 1;
    
    // 重试发送线程池
    private ExecutorService retrySenderExecutor;
    
    private Consumer<Result<SendResult>> resendResultConsumer;
    
    /**
     * 同样消息的Producer，归为同一个Group，应用必须设置，并保证命名唯一
     */
    public RocketMQProducer(String producerGroup, String topic) {
        super(producerGroup, topic);
        producer = new DefaultMQProducer(producerGroup);
        // 默认启用延迟容错，通过统计每个队列的发送耗时情况来计算broker是否可用
        producer.setSendLatencyFaultEnable(true);
    }
    
    /**
     * 同样消息的Producer，归为同一个Group，应用必须设置，并保证命名唯一
     */
    public RocketMQProducer(String producerGroup, String topic, TransactionListener transactionListener) {
        super(producerGroup, topic);
        producer = new TransactionMQProducer(producerGroup);
        // 默认启用延迟容错，通过统计每个队列的发送耗时情况来计算broker是否可用
        producer.setSendLatencyFaultEnable(true);
        ((TransactionMQProducer) producer).setTransactionListener(transactionListener);
    }
    
    /**
     * 启动
     */
    public void start() {
        try {
            // 初始化配置
            initConfig(producer);
            // 数据采样
            if(isSampleEnabled()) {
                // 注册回调钩子
                SohuSendMessageHook hook = new SohuSendMessageHook(producer);
                statsHelper = hook.getStatsHelper();
                statsHelper.setMqCloudDomain(getMqCloudDomain());
                MQMetricsExporter.getInstance().add(statsHelper);
                producer.getDefaultMQProducerImpl().registerSendMessageHook(hook);
            }
            producer.start();
            // 初始化重试线程池
            if (defaultRetryTimes > 0 && retrySenderExecutor == null) {
                retrySenderExecutor = new ThreadPoolExecutor(
                        Runtime.getRuntime().availableProcessors(),
                        Runtime.getRuntime().availableProcessors(),
                        1000 * 60,
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<>(100),
                        new ThreadFactory() {
                            private AtomicInteger threadIndex = new AtomicInteger(0);

                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r,
                                        getGroup() + "-retrySenderExecutor-" + this.threadIndex.incrementAndGet());
                            }
                        });
            }
            logger.info("topic:{} group:{} start", topic, group);
        } catch (MQClientException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 发送消息
     *
     * @param messageMap 消息数据
     * @return 发送结果
     */
    public Result<SendResult> publish(Map<String, Object> messageMap) {
        return publish((Object) messageMap);
    }

    /**
     * 发送消息
     *
     * @param messageObject 消息数据
     * @return 发送结果
     */
    public Result<SendResult> publish(Object messageObject) {
        return publish(messageObject, "");
    }

    /**
     * 发送消息
     *
     * @param messageObject 消息数据
     * @param keys key
     * @return 发送结果
     */
    public Result<SendResult> publishWithException(Object messageObject, String keys) throws Exception {
        Result<SendResult> result = publish(messageObject, keys, null);
        if (result.getException() != null) {
            throw result.getException();
        }
        return result;
    }

    /**
     * 发送消息
     *
     * @param messageObject 消息数据
     * @param keys key
     * @return 发送结果
     */
    public Result<SendResult> publish(Object messageObject, String keys) {
        return publish(messageObject, keys, null);
    }

    /**
     * 发送消息
     *
     * @param messageObject 消息数据
     * @param keys key
     * @param delayLevel 延时级别
     * @return 发送结果
     */
    public Result<SendResult> publish(Object messageObject, String keys, MessageDelayLevel delayLevel) {
        return publish(messageObject, "", keys, delayLevel);
    }
    
    
    /**
     * 构建消息
     * @param messageObject 消息
     * @param tags tags
     * @param keys key
     * @param delayLevel 延时级别
     * @return
     * @throws Exception 
     */
    public Message buildMessage(Object messageObject, String tags, String keys, MessageDelayLevel delayLevel)
            throws Exception {
        byte[] bytes = getMessageSerializer().serialize(messageObject);
        Message message = new Message(topic, tags, keys, bytes);
        if (delayLevel != null) {
            message.setDelayTimeLevel(delayLevel.getLevel());
        }
        return message;
    }
    
    /**
     * 发送消息
     * 
     * @param messageObject 消息数据
     * @param tags tags
     * @param keys key
     * @param delayLevel 延时级别
     * @return 发送结果
     */
    public Result<SendResult> publish(Object messageObject, String tags, String keys, MessageDelayLevel delayLevel) {
        Message message = null;
        try {
            message = buildMessage(messageObject, tags, keys, delayLevel);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new Result<SendResult>(false, e);
        }
        return publish(message);
    }
    
    /**
     * 发送消息
     *
     * @param message 消息
     * @return 发送结果
     */
    public Result<SendResult> publish(Message message) {
        try {
            SendResult sendResult = producer.send(message);
            return new Result<SendResult>(true, sendResult);
        } catch (Exception e) {
            return processException(e);
        }
    }
    
    /**
     * 异常处理
     * 
     * @param e
     * @return
     */
    public Result<SendResult> processException(Exception e) {
        logger.error(e.getMessage(), e);
        if (statsHelper != null) {
            statsHelper.recordException(e);
        }
        return new Result<SendResult>(false, e);
    }
    
    /**
     * 批量发送消息
     *
     * @param message 消息
     * @return 发送结果
     */
    public Result<SendResult> publish(Collection<Message> messages) {
        try {
            SendResult sendResult = producer.send(messages);
            return new Result<SendResult>(true, sendResult);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(statsHelper != null) {
                statsHelper.recordException(e);
            }
            return new Result<SendResult>(false, e);
        }
    }

    /**
     * 发送有序消息
     *
     * @param messageMap 消息数据
     * @param keys key
     * @param arg 回调队列选择器时，此参数会传入队列选择方法
     * @return 发送结果
     */
    public Result<SendResult> publishOrder(Map<String, Object> messageMap, String keys, Object arg) {
        return publishOrder((Object) messageMap, keys, arg);
    }

    /**
     * 发送有序消息
     *
     * @param messageObject 消息数据
     * @param keys key
     * @param arg 回调队列选择器时，此参数会传入队列选择方法
     * @return 发送结果
     */
    public Result<SendResult> publishOrder(Object messageObject, String keys, Object arg) {
        return publishOrder(messageObject, keys, arg, null);
    }

    /**
     * 发送有序消息
     *
     * @param messageObject 消息数据
     * @param keys key
     * @param arg 回调队列选择器时，此参数会传入队列选择方法
     * @param delayLevel 发送延时消息 @MessageDelayLevel
     * @return 发送结果
     */
    public Result<SendResult> publishOrder(Object messageObject, String keys, Object arg, MessageDelayLevel delayLevel) {
        Message message = null;
        try {
            message = buildMessage(messageObject, null, keys, delayLevel);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new Result<SendResult>(false, e);
        }
        return publishOrder(message, arg);
    }
    
    /**
     * 发送有序消息
     *
     * @param message 消息数据
     * @param arg 回调队列选择器时，此参数会传入队列选择方法
     * @return 发送结果
     */
    public Result<SendResult> publishOrder(Message message, Object arg) {
        try {
            SendResult sendResult = producer.send(message, messageQueueSelector, arg);
            return new Result<SendResult>(true, sendResult);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new Result<SendResult>(false, e);
        }
    }

    /**
     * 发送异步消息
     * 
     * @param messageObject 消息
     * 
     * @param keys key
     * @param delayLevel 延时级别
     * @param sendCallback 回调函数
     */
    public void publishAsync(Object messageObject, String keys, MessageDelayLevel delayLevel,
            SendCallback sendCallback) {
        publishAsync(messageObject, "", keys, delayLevel, sendCallback);
    }
    
    /**
     * 发送异步消息
     * 
     * @param messageObject 消息
     * @param tags tags
     * @param keys key
     * @param delayLevel 延时级别
     * @param sendCallback 回调函数
     */
    public void publishAsync(Object messageObject, String tags, String keys, MessageDelayLevel delayLevel,
            SendCallback sendCallback) {
        Message message = null;
        try {
            message = buildMessage(messageObject, tags, keys, delayLevel);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            sendCallback.onException(e);
        }
        publishAsync(message, sendCallback);
    }
    
    /**
     * 发送异步消息
     * 
     * @param message 消息
     * @param sendCallback 回调函数
     */
    public void publishAsync(Message message, final SendCallback sendCallback) {
        try {
            if(statsHelper == null) {
                producer.send(message, sendCallback);
            } else {
                producer.send(message, new SendCallback() {
                    public void onSuccess(SendResult sendResult) {
                        sendCallback.onSuccess(sendResult);
                    }
                    public void onException(Throwable e) {
                        if(statsHelper != null) {
                            statsHelper.recordException(e);
                        }
                        sendCallback.onException(e);
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(statsHelper != null) {
                statsHelper.recordException(e);
            }
            sendCallback.onException(e);
        }
    }

    /**
     * 发送异步消息
     * 
     * @param messageMap 消息数据
     * @param sendCallback 回调函数
     */
    public void publishAsync(Map<String, Object> messageMap, SendCallback sendCallback) {
        publishAsync((Object) messageMap, sendCallback);
    }

    /**
     * 发送异步消息
     * 
     * @param messageMap 消息数据
     * @param sendCallback 回调函数
     */
    public void publishAsync(Object messageObject, SendCallback sendCallback) {
        publishAsync(messageObject, "", sendCallback);
    }

    /**
     * 发送异步消息
     * 异常时将在rocketmq线程进行回调，该方法无法把所有异常抛出，故废弃
     * @param messageMap 消息数据
     * @param messageMap key
     * @param sendCallback 回调函数
     */
    @Deprecated
    public void publishAsyncWithException(Object messageObject, String keys, SendCallback sendCallback)
            throws Exception {
        publishAsyncWithException(messageObject, "", keys, sendCallback);
    }
    
    /**
     * 发送异步消息
     * 异常时将在rocketmq线程进行回调，该方法无法把所有异常抛出，故废弃
     * @param messageObject 消息数据
     * @param tags tags
     * @param String keys
     * @param sendCallback 回调函数
     */
    @Deprecated
    public void publishAsyncWithException(Object messageObject, String tags, String keys, SendCallback sendCallback)
            throws Exception {
        Message message = null;
        try {
            message = buildMessage(messageObject, tags, keys, null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            sendCallback.onException(e);
            throw e;
        }
        publishAsyncWithException(message, sendCallback);
    }
    
    /**
     * 发送异步消息
     * 异常时将在rocketmq线程进行回调，该方法无法把所有异常抛出，故废弃
     * @param messageObject 消息数据
     * @param sendCallback 回调函数
     */
    @Deprecated
    public void publishAsyncWithException(Message message, SendCallback sendCallback)
            throws Exception {
        try {
            producer.send(message, sendCallback);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            sendCallback.onException(e);
            throw e;
        }
    }

    /**
     * 发送异步消息
     * 
     * @param messageMap 消息数据
     * @param messageMap key
     * @param sendCallback 回调函数
     */
    public void publishAsync(Object messageObject, String keys, SendCallback sendCallback) {
        publishAsync(messageObject, keys, null, sendCallback);
    }

    /**
     * 发送Oneway消息
     * 
     * @param messageObject 消息
     * @param keys key
     * @param delayLevel 延时级别
     * @return Result.true or false with exception
     */
    public Result<SendResult> publishOneway(Object messageObject, String keys, MessageDelayLevel delayLevel) {
        return publishOneway(messageObject, "", keys, delayLevel);
    }
    
    /**
     * 发送Oneway消息
     * 
     * @param messageObject 消息
     * @param String tags
     * @param keys key
     * @param delayLevel 延时级别
     * @return Result.true or false with exception
     */
    public Result<SendResult> publishOneway(Object messageObject, String tags, String keys, MessageDelayLevel delayLevel) {
        Message message = null;
        try {
            message = buildMessage(messageObject, tags, keys, delayLevel);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new Result<SendResult>(false, e);
        }
        return publishOneway(message);
    }

    /**
     * 发送Oneway消息
     * 
     * @param message 消息
     * @return Result.true or false with exception
     */
    public Result<SendResult> publishOneway(Message message) {
        try {
            producer.sendOneway(message);
            return new Result<SendResult>(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new Result<SendResult>(false, e);
        }
    }
    
    /**
     * 发送Oneway消息
     * 
     * @param messageMap 消息
     * @return Result.true or false with exception
     */
    public Result<SendResult> publishOneway(Map<String, Object> messageMap) {
        return publishOneway((Object) messageMap);
    }

    /**
     * 发送Oneway消息
     * 
     * @param messageMap 消息
     * @return Result.true or false with exception
     */
    public Result<SendResult> publishOneway(Object messageObject) {
        return publishOneway(messageObject, "");
    }

    /**
     * 发送Oneway消息
     * 
     * @param messageObject 消息
     * @param keys key
     * @return Result.true or false with exception
     */
    public Result<SendResult> publishOnewayWithExcetpion(Object messageObject, String keys) throws Exception {
        Result<SendResult> result = publishOneway(messageObject, keys, null);
        if (result.getException() != null) {
            throw result.getException();
        }
        return result;
    }

    /**
     * 发送Oneway消息
     * 
     * @param messageObject 消息
     * @param keys key
     * @return Result.true or false with exception
     */
    public Result<SendResult> publishOneway(Object messageObject, String keys) {
        return publishOneway(messageObject, keys, null);
    }
    
    /**
     * 发送事务消息
     *
     * @param messageMap 消息数据
     * @param arg 执行本地事务时，回传arg
     * @return 发送结果
     */
    public Result<SendResult> publishTransaction(Map<String, Object> messageMap, Object arg) {
        return publishTransaction(messageMap, null, arg);
    }
    
    /**
     * 发送事务消息
     *
     * @param messageMap 消息数据
     * @param keys key
     * @param arg 执行本地事务时，回传arg
     * @return 发送结果
     */
    public Result<SendResult> publishTransaction(Map<String, Object> messageMap, String keys, Object arg) {
        return publishTransaction((Object) messageMap, keys, arg);
    }
    
    /**
     * 发送事务消息
     *
     * @param messageMap 消息数据
     * @param arg 执行本地事务时，回传arg
     * @return 发送结果
     */
    public Result<SendResult> publishTransaction(Object messageObject, Object arg) {
        return publishTransaction(messageObject, null, arg);
    }
    
    /**
     * 发送事务消息
     *
     * @param messageObject 消息数据
     * @param keys key
     * @return 发送结果
     */
    public Result<SendResult> publishTransaction(Object messageObject, String keys) {
        return publishTransaction(messageObject, keys, null);
    }

    /**
     * 发送事务消息
     *
     * @param messageObject 消息数据
     * @param keys key
     * @param arg 执行本地事务时，回传arg
     * @return 发送结果
     */
    public Result<SendResult> publishTransaction(Object messageObject, String keys, Object arg) {
        return publishTransaction(messageObject, null, keys, arg);
    }
    
    /**
     * 发送事务消息
     *
     * @param messageObject 消息数据
     * @param keys key
     * @param arg 执行本地事务时，回传arg
     * @return 发送结果
     */
    public Result<SendResult> publishTransaction(Object messageObject, String tags, String keys, Object arg) {
        Message message = null;
        try {
            message = buildMessage(messageObject, tags, keys, null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new Result<SendResult>(false, e);
        }
        return publishTransaction(message, arg);
    }
    
    /**
     * 发送事务消息消息
     *
     * @param message 消息
     * @param arg 执行本地事务时，回传arg
     * @return 发送结果
     */
    public Result<SendResult> publishTransaction(Message message, Object arg) {
        try {
            SendResult sendResult = ((TransactionMQProducer) producer).sendMessageInTransaction(message, arg);
            return new Result<SendResult>(true, sendResult);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new Result<SendResult>(false, e);
        }
    }
    
    /**
     * 发送消息
     *
     * @param message 消息
     * @return 发送结果
     */
    @SuppressWarnings("rawtypes")
    public Result<SendResult> send(MQMessage mqMessage) {
        // 无body，序列化
        if (mqMessage.getBody() == null) {
            try {
                mqMessage.setBody(getMessageSerializer().serialize(mqMessage.getMessage()));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return new Result<SendResult>(false, e);
            }
        }
        // 设置属性
        mqMessage.setTopic(getTopic());
        mqMessage.resetRetryTimes(this.defaultRetryTimes);
        // 消息发送
        try {
            SendResult sendResult = producer.send(mqMessage.getInnerMessage());
            if (mqMessage.isExceptionForTest()) {
                logger.info("send ok: msgId:{} offsetMsgId:{}", sendResult.getMsgId(), sendResult.getOffsetMsgId());
                throw new RemotingException("exceptionForTest");
            }
            return new Result<SendResult>(true, sendResult);
        } catch (MQClientException e) {
            return processException(e);
        } catch (Exception e) {
            // 重试
            if (mqMessage.getRetryTimes() > 0 && resend(mqMessage)) {
                return new Result<SendResult>(false, e).setRetrying(true);
            } else {
                return processException(e);
            }
        }
    }
    
    /**
     * 重试发送
     * 
     * @param message
     * @return
     */
    @SuppressWarnings("rawtypes")
    private boolean resend(MQMessage mqMessage) {
        try {
            retrySenderExecutor.execute(() -> {
                Result<SendResult> result = null;
                try {
                    result = _resend(mqMessage);
                } catch (Throwable e) {
                    result = new Result<>(false, new MQClientException(e.toString(), e));
                }
                result.setMqMessage(mqMessage);
                // 处理重发消息结果
                processResendResult(result);
            });
            return true;
        } catch (RejectedExecutionException e) {
            logger.warn("reject retryPublish...");
            return false;
        }
    }

    /**
     * 重试发送
     * 
     * @param message
     * @return
     */
    @SuppressWarnings("rawtypes")
    private Result<SendResult> _resend(MQMessage mqMessage) {
        Exception exception = null;
        // 循环重试发送
        for (int i = 1; i <= mqMessage.getRetryTimes(); ++i) {
            try {
                SendResult sendResult = producer.send(mqMessage.getInnerMessage());
                Result<SendResult> result = new Result<>(true, sendResult);
                result.setRetriedTimes(i);
                return result;
            } catch (Exception e) {
                exception = e;
            }
        }
        // 发送失败，记录结果
        if (statsHelper != null) {
            statsHelper.recordException(exception);
        }
        Result<SendResult> result = new Result<>(false, exception);
        result.setRetriedTimes(mqMessage.getRetryTimes());
        return result;
    }
    
    /**
     * 处理重发结果
     * 
     * @param result
     */
    private void processResendResult(Result<SendResult> result) {
        // 有重试消费者
        if (resendResultConsumer != null) {
            try {
                resendResultConsumer.accept(result);
            } catch (Exception e) {
                logger.error("resendResultConsumer consume:{} error", result, e);
            }
        }
        // 无重试消费者记录日志
        if (!result.isSuccess()) {
            logger.error("retryTimes:{} message:{} error!", result.getRetriedTimes(),
                    result.getMqMessage().getMessage(), result.getException());
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("retryTimes:{} message:{} success", result.getRetriedTimes(),
                        result.getMqMessage().getMessage());
            }
        }
    }
    
    public int getDefaultRetryTimes() {
        return defaultRetryTimes;
    }

    public void setDefaultRetryTimes(int defaultRetryTimes) {
        this.defaultRetryTimes = defaultRetryTimes;
    }

    public ExecutorService getRetrySenderExecutor() {
        return retrySenderExecutor;
    }

    public void setRetrySenderExecutor(ExecutorService retrySenderExecutor) {
        this.retrySenderExecutor = retrySenderExecutor;
    }

    public Consumer<Result<SendResult>> getResendResultConsumer() {
        return resendResultConsumer;
    }

    public void setResendResultConsumer(Consumer<Result<SendResult>> resendResultConsumer) {
        this.resendResultConsumer = resendResultConsumer;
    }

    public void shutdown() {
        producer.shutdown();
        if (statsHelper != null) {
            statsHelper.shutdown();
        }
        if (retrySenderExecutor != null) {
            retrySenderExecutor.shutdown();
        }
        super.shutdown();
    }

    public DefaultMQProducer getProducer() {
        return producer;
    }

    /**
     * 参考@com.alibaba.rocketmq.store.config.MessageStoreConfig.messageDelayLevel定义
     */
    public enum MessageDelayLevel {
        LEVEL_1_SECOND(1, 1000), 
        LEVEL_5_SECONDS(2, 5000), 
        LEVEL_10_SECONDS(3, 10000), 
        LEVEL_30_SECONDS(4, 30000), 
        LEVEL_1_MINUTE(5, 60000), 
        LEVEL_2_MINUTES(6, 120000), 
        LEVEL_3_MINUTES(7, 180000), 
        LEVEL_4_MINUTES(8, 240000), 
        LEVEL_5_MINUTES(9, 300000), 
        LEVEL_6_MINUTES(10, 360000), 
        LEVEL_7_MINUTES(11, 420000), 
        LEVEL_8_MINUTES(12, 480000), 
        LEVEL_9_MINUTES(13, 540000), 
        LEVEL_10_MINUTES(14, 600000), 
        LEVEL_20_MINUTES(15, 1200000), 
        LEVEL_30_MINUTES(16, 1800000), 
        LEVEL_1_HOUR(17, 3600000), 
        LEVEL_2_HOURS(18, 7200000),
        ;

        private int level;
        private long delayTimeMillis;

        private MessageDelayLevel(int level, long delayTimeMillis) {
            this.level = level;
            this.delayTimeMillis = delayTimeMillis;
        }

        public static MessageDelayLevel findByLevel(int level) {
            for (MessageDelayLevel lev : values()) {
                if (level == lev.getLevel()) {
                    return lev;
                }
            }
            return null;
        }

        public long getDelayTimeMillis() {
            return delayTimeMillis;
        }

        public int getLevel() {
            return level;
        }
    }
    
    public StatsHelper getStatsHelper() {
        return statsHelper;
    }

    public MessageQueueSelector getMessageQueueSelector() {
        return messageQueueSelector;
    }

    public void setMessageQueueSelector(MessageQueueSelector messageQueueSelector) {
        this.messageQueueSelector = messageQueueSelector;
    }

    @Override
    protected int role() {
        return PRODUCER;
    }

    @Override
    protected void registerTraceDispatcher(AsyncTraceDispatcher traceDispatcher) {
        producer.getDefaultMQProducerImpl().registerSendMessageHook(
                new SendMessageTraceHookImpl(traceDispatcher));
    }
    
    public int getSendMsgTimeout() {
        return producer.getSendMsgTimeout();
    }

    public void setSendMsgTimeout(int sendMsgTimeout) {
        producer.setSendMsgTimeout(sendMsgTimeout);
    }
    
    public int getRetryTimesWhenSendFailed() {
        return producer.getRetryTimesWhenSendFailed();
    }

    public void setRetryTimesWhenSendFailed(int retryTimesWhenSendFailed) {
        producer.setRetryTimesWhenSendFailed(retryTimesWhenSendFailed);
    }
}
