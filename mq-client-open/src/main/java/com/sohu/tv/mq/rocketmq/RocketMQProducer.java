package com.sohu.tv.mq.rocketmq;

import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.common.AbstractConfig;
import com.sohu.tv.mq.common.MQRateLimitException;
import com.sohu.tv.mq.common.SohuSendMessageHook;
import com.sohu.tv.mq.dto.WebResult;
import com.sohu.tv.mq.metric.MQMetricsExporter;
import com.sohu.tv.mq.rocketmq.circuitbreaker.SentinelCircuitBreaker;
import com.sohu.tv.mq.route.AffinityMQStrategy;
import com.sohu.tv.mq.stats.StatsHelper;
import com.sohu.tv.mq.trace.SendMessageWithBornHostTraceHookImpl;
import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.impl.producer.TopicPublishInfo;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.client.trace.hook.SendMessageTraceHookImpl;
import org.apache.rocketmq.common.ServiceState;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.protocol.ResponseCode;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * rocketmq producer 封装
 * 
 * @date 2018年1月17日
 * @author copy from indexmq
 */
@SuppressWarnings("deprecation")
public class RocketMQProducer extends AbstractConfig {
    // rocketmq 实际生产者
    private DefaultMQProducer producer;
    
    // 统计助手
    private StatsHelper statsHelper;
    
    // 发送顺序消息使用
    private MessageQueueSelector messageQueueSelector;
    
    // 默认重试次数
    private int defaultRetryTimes = 0;
    
    // 重试发送线程池
    private ExecutorService retrySenderExecutor;

    // 同步发送异步重试结果回调
    private Consumer<Result<SendResult>> resendResultConsumer;

    // 限流发生时，是否暂停一会发送线程
    private boolean suspendAWhileWhenRateLimited = false;

    // 启动时是否获取topic路由信息（用于启动后发送消息前自动与ns和broker建联）
    private boolean fetchTopicRouteInfoWhenStart = true;

    // 是否开启熔断，默认不开启
    private boolean enableCircuitBreaker;

    // 熔断器
    private SentinelCircuitBreaker sentinelCircuitBreaker;

    // 降级回调
    private Consumer<MQMessage> circuitBreakerFallbackConsumer;

    public RocketMQProducer() {
    }

    /**
     * 同样消息的Producer，归为同一个Group，应用必须设置，并保证命名唯一
     */
    public RocketMQProducer(String producerGroup, String topic) {
        construct(producerGroup, topic);
    }
    
    /**
     * 同样消息的Producer，归为同一个Group，应用必须设置，并保证命名唯一
     */
    public RocketMQProducer(String producerGroup, String topic, TransactionListener transactionListener) {
        construct(producerGroup, topic, transactionListener);
    }

    /**
     * 初始化
     */
    public RocketMQProducer construct(String producerGroup, String topic) {
        return construct(producerGroup, topic, null);
    }

    /**
     * 初始化
     */
    public RocketMQProducer construct(String producerGroup, String topic, TransactionListener transactionListener) {
        setGroup(producerGroup);
        setTopic(topic);
        if (transactionListener == null) {
            producer = new DefaultMQProducer(group);
        } else {
            TransactionMQProducer producer = new TransactionMQProducer(group);
            producer.setTransactionListener(transactionListener);
            this.producer = producer;
        }
        // 默认启用延迟容错，通过统计每个队列的发送耗时情况来计算broker是否可用
        producer.setSendLatencyFaultEnable(true);
        return this;
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
            if (fetchTopicRouteInfoWhenStart) {
                producer.getDefaultMQProducerImpl().updateTopicPublishInfo(getTopic(), new TopicPublishInfo());
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
            // init after start
            initAfterStart();
            logger.info("topic:{} group:{} start", topic, group);
        } catch (MQClientException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void initAfterStart() {
        if (statsHelper != null) {
            statsHelper.setClientId(getMQClientInstance().getClientId());
        }
        if (enableCircuitBreaker) {
            sentinelCircuitBreaker = new SentinelCircuitBreaker(topic, circuitBreakerFallbackConsumer);
        }
    }

    /**
     * 构建消息
     */
    public Message buildMessage(Object messageObject) throws Exception {
        return buildMessage(messageObject, null, null, null, null);
    }

    /**
     * 构建消息
     */
    public Message buildMessage(Object messageObject, String clientHost) throws Exception {
        return buildMessage(messageObject, null, null, clientHost, null);
    }

    /**
     * 构建消息
     */
    public Message buildMessage(Object messageObject, String tags, String keys, String clientHost,
                                MessageDelayLevel delayLevel) throws Exception {
        MQMessage mqMessage = MQMessage.build(messageObject).setTopic(getTopic()).setTags(tags).setKeys(keys)
                .setClientHost(clientHost).serialize(getMessageSerializer());
        if (mqMessage.getBody().length > producer.getMaxMessageSize()) {
            throw new MQClientException(ResponseCode.MESSAGE_ILLEGAL,
                    "the message body size over max value, MAX: " + producer.getMaxMessageSize());
        }
        if (delayLevel != null) {
            mqMessage.setDelayTimeLevel(delayLevel.getLevel());
        }
        return mqMessage.getInnerMessage();
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
        return publish(messageObject, null);
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
        return publish(messageObject, null, keys, delayLevel);
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
        MQMessage mqMessage = MQMessage.build(messageObject).setTags(tags).setKeys(keys);
        if (delayLevel != null) {
            mqMessage.setDelayTimeLevel(delayLevel.getLevel());
        }
        return send(mqMessage);
    }
    
    /**
     * 发送消息
     *
     * @param message 消息
     * @return 发送结果
     */
    public Result<SendResult> publish(Message message) {
        return send(MQMessage.build(message));
    }
    
    /**
     * 异常处理
     * 
     * @param e
     * @return
     */
    public Result<SendResult> processException(Throwable e) {
        logger.error("send error", e);
        if (statsHelper != null) {
            statsHelper.recordException(e);
        }
        // 限流后暂停一会
        if (suspendAWhileWhenRateLimited && MQRateLimitException.isRateLimited(e)) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException interruptedException) {
                logger.warn("suspendAWhileWhenRateLimited interrupted");
            }
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
            return processException(e);
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
        MQMessage mqMessage = MQMessage.build(messageObject).setKeys(keys).setOrderArg(arg);
        if (delayLevel != null) {
            mqMessage.setDelayTimeLevel(delayLevel.getLevel());
        }
        return send(mqMessage);
    }
    
    /**
     * 发送有序消息
     *
     * @param message 消息数据
     * @param arg 回调队列选择器时，此参数会传入队列选择方法
     * @return 发送结果
     */
    public Result<SendResult> publishOrder(Message message, Object arg) {
        return send(MQMessage.build(message).setOrderArg(arg));
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
        publishAsync(messageObject, null, keys, delayLevel, sendCallback);
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
        MQMessage mqMessage = MQMessage.build(messageObject).setTags(tags).setKeys(keys).setSendCallback(sendCallback);
        if (delayLevel != null) {
            mqMessage.setDelayTimeLevel(delayLevel.getLevel());
        }
        send(mqMessage);
    }
    
    /**
     * 发送异步消息
     * 
     * @param message 消息
     * @param sendCallback 回调函数
     */
    public void publishAsync(Message message, final SendCallback sendCallback) {
        send(MQMessage.build(message).setSendCallback(sendCallback));
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
        publishAsync(messageObject, null, sendCallback);
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
    @Deprecated
    public Result<SendResult> publishOneway(Object messageObject, String keys, MessageDelayLevel delayLevel) {
        return publishOneway(messageObject, null, keys, delayLevel);
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
    @Deprecated
    public Result<SendResult> publishOneway(Object messageObject, String tags, String keys, MessageDelayLevel delayLevel) {
        MQMessage mqMessage = MQMessage.build(messageObject).setTags(tags).setKeys(keys);
        if (delayLevel != null) {
            mqMessage.setDelayTimeLevel(delayLevel.getLevel());
        }
        return send(mqMessage);
    }

    /**
     * 发送Oneway消息
     * 
     * @param message 消息
     * @return Result.true or false with exception
     */
    @Deprecated
    public Result<SendResult> publishOneway(Message message) {
        return send(MQMessage.build(message).setOneWay(true));
    }
    
    /**
     * 发送Oneway消息
     * 
     * @param messageMap 消息
     * @return Result.true or false with exception
     */
    @Deprecated
    public Result<SendResult> publishOneway(Map<String, Object> messageMap) {
        return publishOneway((Object) messageMap);
    }

    /**
     * 发送Oneway消息
     * 
     * @param messageMap 消息
     * @return Result.true or false with exception
     */
    @Deprecated
    public Result<SendResult> publishOneway(Object messageObject) {
        return publishOneway(messageObject, null);
    }

    /**
     * 发送Oneway消息
     * 
     * @param messageObject 消息
     * @param keys key
     * @return Result.true or false with exception
     */
    @Deprecated
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
        return send(MQMessage.build(messageObject).setTags(tags).setKeys(keys).setTransaction(true).setTransactionArg(arg));
    }
    
    /**
     * 发送事务消息消息
     *
     * @param message 消息
     * @param arg 执行本地事务时，回传arg
     * @return 发送结果
     */
    public Result<SendResult> publishTransaction(Message message, Object arg) {
        return send(MQMessage.build(message).setTransaction(true).setTransactionArg(arg));
    }

    /**
     * 发送消息
     *
     * @param mqMessage 消息
     * @return 发送结果
     */
    public Result<SendResult> send(MQMessage mqMessage) {
        try {
            beforeMessageSend(mqMessage);
            return _send(mqMessage);
        } catch (Exception e) {
            return messageSendError(mqMessage, e);
        } finally {
            if (mqMessage.isEnableCircuitBreaker()) {
                sentinelCircuitBreaker.exit(mqMessage);
            }
        }
    }

    private void beforeMessageSend(MQMessage mqMessage) throws Exception {
        // 无body，序列化
        if (mqMessage.getBody() == null) {
            mqMessage.setBody(getMessageSerializer().serialize(mqMessage.getMessage()));
        }
        // 设置属性
        mqMessage.setTopic(getTopic());
        mqMessage.resetRetryTimes(this.defaultRetryTimes);
        mqMessage.resetEnableCircuitBreaker(this.enableCircuitBreaker);
        // 获取熔断器资源
        if (mqMessage.isEnableCircuitBreaker()) {
            sentinelCircuitBreaker.entry(mqMessage);
        }
    }

    private Result _send(MQMessage mqMessage) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        SendResult sendResult = null;
        if (mqMessage.getOrderArg() != null) {
            // 顺序发送
            sendResult = producer.send(mqMessage.getInnerMessage(), messageQueueSelector, mqMessage.getOrderArg());
        } else if (mqMessage.getSendCallback() != null) {
            // 异步发送
            _asyncSend(mqMessage);
        } else if (mqMessage.isOneWay()) {
            // 单向发送
            producer.sendOneway(mqMessage.getInnerMessage());
        } else if (mqMessage.isTransaction()) {
            // 事务消息
            sendResult = ((TransactionMQProducer) producer).sendMessageInTransaction(mqMessage.getInnerMessage(),
                    mqMessage.getTransactionArg());
        } else {
            // 同步发送
            sendResult = producer.send(mqMessage.getInnerMessage());
        }
        afterMessageSend(mqMessage, sendResult);
        return new Result<SendResult>(true, sendResult);
    }

    private void _asyncSend(MQMessage mqMessage) throws RemotingException, InterruptedException, MQClientException {
        if (statsHelper == null) {
            producer.send(mqMessage.getInnerMessage(), mqMessage.getSendCallback());
        } else {
            producer.send(mqMessage.getInnerMessage(), new SendCallback() {
                public void onSuccess(SendResult sendResult) {
                    mqMessage.getSendCallback().onSuccess(sendResult);
                }

                public void onException(Throwable e) {
                    processException(e);
                    mqMessage.getSendCallback().onException(e);
                }
            });
        }
    }

    private void afterMessageSend(MQMessage mqMessage, SendResult sendResult) throws RemotingException {
        if (mqMessage.isExceptionForTest()) {
            logger.info("send ok: msgId:{} offsetMsgId:{}", sendResult.getMsgId(), sendResult.getOffsetMsgId());
            throw new RemotingException("exceptionForTest");
        }
    }

    private Result<SendResult> messageSendError(MQMessage mqMessage, Exception e) {
        if (mqMessage.isEnableCircuitBreaker()) {
            sentinelCircuitBreaker.trace(e, mqMessage);
        }
        if (mqMessage.getSendCallback() != null) {
            mqMessage.getSendCallback().onException(e);
        }
        if (mqMessage.needRetry(e) && resend(mqMessage)) { // 重试
            return new Result<SendResult>(false, e).setRetrying(true);
        }
        return processException(e);
    }

    /**
     * 取消延迟消息
     *
     * @param uniqueId 消息ID
     * @param token token
     * @return 发送结果
     */
    public WebResult<String> cancelDelayedMessage(String uniqueId, String token) {
        DefaultMQProducerImpl defaultMQProducerImpl = getProducer().getDefaultMQProducerImpl();
        if (defaultMQProducerImpl.getServiceState() != ServiceState.RUNNING) {
            return WebResult.setFail(500, "producer is not running");
        }
        return CommonUtil.cancelDelayedMsg(topic, uniqueId, token, getMqCloudDomain());
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
        producer.getDefaultMQProducerImpl().registerSendMessageHook(new SendMessageTraceHookImpl(traceDispatcher));
        producer.getDefaultMQProducerImpl().registerSendMessageHook(new SendMessageWithBornHostTraceHookImpl());
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

    public boolean isSuspendAWhileWhenRateLimited() {
        return suspendAWhileWhenRateLimited;
    }

    public void setSuspendAWhileWhenRateLimited(boolean suspendAWhileWhenRateLimited) {
        this.suspendAWhileWhenRateLimited = suspendAWhileWhenRateLimited;
    }

    public boolean isFetchTopicRouteInfoWhenStart() {
        return fetchTopicRouteInfoWhenStart;
    }

    public void setFetchTopicRouteInfoWhenStart(boolean fetchTopicRouteInfoWhenStart) {
        this.fetchTopicRouteInfoWhenStart = fetchTopicRouteInfoWhenStart;
    }

    public boolean isEnableCircuitBreaker() {
        return enableCircuitBreaker;
    }

    public void setEnableCircuitBreaker(boolean enableCircuitBreaker) {
        this.enableCircuitBreaker = enableCircuitBreaker;
    }

    public Consumer<MQMessage> getCircuitBreakerFallbackConsumer() {
        return circuitBreakerFallbackConsumer;
    }

    public void setCircuitBreakerFallbackConsumer(Consumer<MQMessage> circuitBreakerFallbackConsumer) {
        this.circuitBreakerFallbackConsumer = circuitBreakerFallbackConsumer;
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

    public MQClientInstance getMQClientInstance() {
        return producer.getDefaultMQProducerImpl().getMqClientFactory();
    }
}
