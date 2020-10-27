package com.sohu.tv.mq.rocketmq;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.PullMessageService;
import org.apache.rocketmq.client.impl.consumer.PullRequest;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.client.trace.hook.ConsumeMessageTraceHookImpl;
import org.apache.rocketmq.common.ServiceState;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.common.utils.HttpTinyClient;
import org.apache.rocketmq.common.utils.HttpTinyClient.HttpResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sohu.index.tv.mq.common.BatchConsumerCallback;
import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.tv.mq.common.AbstractConfig;
import com.sohu.tv.mq.dto.ConsumerConfigDTO;
import com.sohu.tv.mq.dto.DTOResult;
import com.sohu.tv.mq.rocketmq.limiter.LeakyBucketRateLimiter;
import com.sohu.tv.mq.rocketmq.limiter.RateLimiter;
import com.sohu.tv.mq.rocketmq.limiter.SwitchableRateLimiter;
import com.sohu.tv.mq.rocketmq.limiter.TokenBucketRateLimiter;
import com.sohu.tv.mq.util.Constant;

/**
 * rocketmq 消费者
 * 
 * @Description: push封装
 * @author copy from indexmq
 * @date 2018年1月17日
 */
@SuppressWarnings("deprecation")
public class RocketMQConsumer extends AbstractConfig {

    // 支持一批消息消费
    private BatchConsumerCallback<?, MessageExt> batchConsumerCallback;

    /**
     * 消费者
     */
    private DefaultMQPushConsumer consumer;

    @SuppressWarnings("rawtypes")
    private ConsumerCallback consumerCallback;

    /**
     * 是否重试
     */
    private boolean reconsume = true;

    /**
     * 是否debug
     */
    private boolean debug;

    // "tag1 || tag2 || tag3"
    private String subExpression = "*";

    // 是否顺序消费
    private boolean consumeOrderly = false;

    // 跳过重试消息时间，默认为-1，即不跳过
    private volatile long retryMessageResetTo = -1;
    
    // 消息限速器
    private RateLimiter rateLimiter;
    
    private ScheduledExecutorService clientConfigScheduledExecutorService;
    
    private Class<?> consumerParameterTypeClass;
    
    // 关闭等待最大时间
    private long shutdownWaitMaxMillis = 30000;

    /**
     * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例<br>
     * ConsumerGroupName需要由应用来保证唯一
     */
    public RocketMQConsumer(String consumerGroup, String topic) {
        this(consumerGroup, topic, false);
    }
    
    /**
     * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例<br>
     * ConsumerGroupName需要由应用来保证唯一
     */
    public RocketMQConsumer(String consumerGroup, String topic, boolean useLeakyBucketRateLimiter) {
        super(consumerGroup, topic);
        consumer = new DefaultMQPushConsumer(consumerGroup);
        // 消费消息超时将会发回重试队列，超时时间由默认的15分钟修改为2小时
        consumer.setConsumeTimeout(2 * 60);
        // 初始化限速器
        if (useLeakyBucketRateLimiter) {
            initLeakyBucketRateLimiter();
        } else {
            initTokenBucketRateLimiter();
        }
    }

    public void start() {
        try {
            // 初始化配置
            initConfig(consumer);
            if (getClusterInfoDTO().isBroadcast()) {
                consumer.setMessageModel(MessageModel.BROADCASTING);
            }
            consumer.subscribe(topic, subExpression);

            // 构建消费者对象
            final MessageConsumer messageConsumer = new MessageConsumer(this);
            // 注册顺序或并发消费
            if (consumeOrderly) {
                consumer.registerMessageListener(new MessageListenerOrderly() {
                    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                        return messageConsumer.consumeMessage(msgs, context);
                    }
                });
            } else {
                consumer.registerMessageListener(new MessageListenerConcurrently() {
                    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                            ConsumeConcurrentlyContext context) {
                        return messageConsumer.consumeMessage(msgs, context);
                    }
                });
            }
            // 初始化消费者参数类型
            initConsumerParameterTypeClass();
            // 初始化定时调度任务
            initScheduleTask();
            // 消费者启动
            consumer.start();
            logger.info("topic:{} group:{} start", topic, group);
        } catch (MQClientException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 从mqcloud更新动态配置
     */
    private void initScheduleTask() {
        // 数据采样线程
        clientConfigScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "updateConsumerConfigThread-" + getGroup());
            }
        });
        clientConfigScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpResult result = HttpTinyClient.httpGet(
                            "http://" + getMqCloudDomain() + "/consumer/config/" + getGroup(), null, null, "UTF-8", 5000);
                    if (HttpURLConnection.HTTP_OK != result.code) {
                        logger.error("http response err: code:{},info:{}", result.code, result.content);
                        return;
                    }
                    DTOResult<ConsumerConfigDTO> dtoResult = JSON.parseObject(result.content, new TypeReference<DTOResult<ConsumerConfigDTO>>(){});
                    ConsumerConfigDTO consumerConfigDTO = dtoResult.getResult();
                    if(consumerConfigDTO == null) {
                        return;
                    }
                    // 1.更新重试跳过时间戳
                    if (consumerConfigDTO.getRetryMessageResetTo() != null && 
                            retryMessageResetTo != consumerConfigDTO.getRetryMessageResetTo()) {
                        setRetryMessageResetTo(consumerConfigDTO.getRetryMessageResetTo());
                    }
                    // 2.更新消费是否暂停
                    boolean needCheckPause = false;
                    if (consumerConfigDTO.getPause() != null) {
                        String pauseClientId = consumerConfigDTO.getPauseClientId();
                        // 停止所有实例
                        if (pauseClientId == null || pauseClientId.length() == 0) {
                            needCheckPause = true;
                        } else if (consumerConfigDTO.getPauseClientId()
                                .equals(consumer.getDefaultMQPushConsumerImpl().getmQClientFactory().getClientId())) { // 只停止当前实例
                            needCheckPause = true;
                        }
                    }
                    if (needCheckPause && consumer.getDefaultMQPushConsumerImpl().isPause() != consumerConfigDTO.getPause()) {
                        setPause(consumerConfigDTO.getPause());
                    }
                    // 3.更新限速
                    if (consumerConfigDTO.getEnableRateLimit() != null && 
                            isEnableRateLimit() != consumerConfigDTO.getEnableRateLimit()) {
                        setEnableRateLimit(consumerConfigDTO.getEnableRateLimit());
                    }
                    if (consumerConfigDTO.getPermitsPerSecond() != null) {
                        int rate = consumerConfigDTO.getPermitsPerSecond().intValue();
                        if (getRate() != rate) {
                            setRate(rate);
                        }
                    }
                } catch (Throwable ignored) {
                    logger.warn("skipRetryMessage err:{}", ignored);
                }
            }
        }, 5, 60, TimeUnit.SECONDS);
    }

    public void shutdown() {
        DefaultMQPushConsumerImpl innerConsumer = consumer.getDefaultMQPushConsumerImpl();
		if (ServiceState.RUNNING != innerConsumer.getServiceState()) {
			logger.info("conusmer:{} state is {}, no need shutdown", getGroup(), innerConsumer.getServiceState());
			return;
		}
        // 1.首先关闭rebalance线程，不再接受新的队列变更等。
        ServiceThread thread = getField(MQClientInstance.class, "rebalanceService", innerConsumer.getmQClientFactory());
        thread.shutdown();
        // 2.接着标记拉取线程停止，不直接关闭是为了把拉下来的消息消费完毕。
        PullMessageService pull = innerConsumer.getmQClientFactory().getPullMessageService();
        pull.makeStop();
        // 3.根据拉取任务数是否与处理队列数相等，来判断消息是否已经消费完毕；超过30秒则不再等待
        long start = System.currentTimeMillis();
        LinkedBlockingQueue<PullRequest> q = getField(PullMessageService.class, "pullRequestQueue", pull);
        int pullRequestSize = getPullRequestSize(q);
        while (pullRequestSize != innerConsumer.getRebalanceImpl().getProcessQueueTable().size()) {
            long use = System.currentTimeMillis() - start;
            if (use > getShutdownWaitMaxMillis()) {
                logger.warn("{} shutdown too long, use:{}ms, break!!, pullRequestQueueSize:{} processQueueTableSize:{}",
                        getGroup(), use, pullRequestSize, innerConsumer.getRebalanceImpl().getProcessQueueTable().size());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.warn("ignore interrupted!!");
            }
            pullRequestSize = getPullRequestSize(q);
        }
        // 4.如下为正常关闭流程
        consumer.shutdown();
        rateLimiter.shutdown();
        clientConfigScheduledExecutorService.shutdown();
    }
    
    private int getPullRequestSize(LinkedBlockingQueue<PullRequest> q) {
        if (q == null) {
            return 0;
        }
        int size = 0;
        for (PullRequest pullRequest : q) {
            if (getGroup().equals(pullRequest.getConsumerGroup())) {
                ++size;
            }
        }
        return size;
    }
    
    /**
     * 获取类的字段实例
     * @param clz
     * @param field
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> T getField(Class<?> clz, String field, Object obj){
        try {
            Field f = clz.getDeclaredField(field);
            f.setAccessible(true);
            return(T) f.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Batch consumption size
     * 
     * @param consumeMessageBatchMaxSize
     */
    public void setConsumeMessageBatchMaxSize(int consumeMessageBatchMaxSize) {
        if (consumeMessageBatchMaxSize <= 0) {
            return;
        }
        consumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        consumer.setConsumeFromWhere(consumeFromWhere);
    }

    public void setReconsume(boolean reconsume) {
        this.reconsume = reconsume;
    }

    @SuppressWarnings("rawtypes")
    public void setConsumerCallback(ConsumerCallback consumerCallback) {
        this.consumerCallback = consumerCallback;
    }

    public void setConsumeTimestamp(String consumeTimestamp) {
        consumer.setConsumeTimestamp(consumeTimestamp);
    }

    public DefaultMQPushConsumer getConsumer() {
        return consumer;
    }

    /**
     * 消费线程数，默认20
     * 
     * @param num
     */
    public void setConsumeThreadMin(int num) {
        if (num <= 0) {
            return;
        }
        consumer.setConsumeThreadMin(num);
    }

    /**
     * 消费线程数，默认64
     * 
     * @param num
     */
    public void setConsumeThreadMax(int num) {
        if (num <= 0) {
            return;
        }
        consumer.setConsumeThreadMax(num);
    }

    /**
     * 一次拉取多少个消息 ，默认32
     * 
     * @param size
     */
    public void setPullBatchSize(int size) {
        if (size < 0) {
            return;
        }
        consumer.setPullBatchSize(size);
    }

    /**
     * queue中缓存多少个消息时进行流控 ，默认1000
     * 
     * @param size
     */
    public void setPullThresholdForQueue(int size) {
        if (size < 0) {
            return;
        }
        consumer.setPullThresholdForQueue(size);
    }

    /**
     * queue中缓存多少M消息时进行流控 ，默认100
     * 
     * @param size
     */
    public void setPullThresholdSizeForQueue(int size) {
        if (size < 0) {
            return;
        }
        consumer.setPullThresholdSizeForQueue(size);
    }

    /**
     * topic维度缓存多少个消息时进行流控 ，默认-1，不限制
     * 
     * @param size
     */
    public void setPullThresholdForTopic(int size) {
        if (size < 0) {
            return;
        }
        consumer.setPullThresholdForTopic(size);
    }

    /**
     * topic维度缓存多少M消息时进行流控 ，默认-1，不限制
     * 
     * @param size
     */
    public void setPullThresholdSizeForTopic(int size) {
        if (size < 0) {
            return;
        }
        consumer.setPullThresholdSizeForTopic(size);
    }

    /**
     * 拉取消息的时间间隔，毫秒，默认为0
     * 
     * @param pullInterval
     */
    public void setPullInterval(int pullInterval) {
        consumer.setPullInterval(pullInterval);
    }

    @SuppressWarnings("rawtypes")
    public ConsumerCallback getConsumerCallback() {
        return consumerCallback;
    }

    @SuppressWarnings("unchecked")
    public <T> BatchConsumerCallback<T, MessageExt> getBatchConsumerCallback() {
        return (BatchConsumerCallback<T, MessageExt>) batchConsumerCallback;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setBatchConsumerCallback(BatchConsumerCallback batchConsumerCallback) {
        this.batchConsumerCallback = batchConsumerCallback;
    }

    /**
     * 1.8.3之后不用设置broadcast了，可以自动区分
     * 
     * @param broadcast
     */
    @Deprecated
    public void setBroadcast(boolean broadcast) {
    }

    public String getSubExpression() {
        return subExpression;
    }

    public void setSubExpression(String subExpression) {
        this.subExpression = subExpression;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isReconsume() {
        return reconsume;
    }

    public void setConsumeOrderly(boolean consumeOrderly) {
        this.consumeOrderly = consumeOrderly;
    }

    @Override
    protected int role() {
        return CONSUMER;
    }

    @Override
    protected void registerTraceDispatcher(AsyncTraceDispatcher traceDispatcher) {
        consumer.getDefaultMQPushConsumerImpl().registerConsumeMessageHook(
                new ConsumeMessageTraceHookImpl(traceDispatcher));
    }

    /**
     * traceEnabled is controlled by MQCloud
     * 
     * @param traceEnabled
     */
    @Deprecated
    public void setTraceEnabled(boolean traceEnabled) {
    }

    /**
     * Maximum amount of time in minutes a message may block the consuming
     * thread.
     */
    public void setConsumeTimeout(long consumeTimeout) {
        if (consumeTimeout <= 0) {
            return;
        }
        consumer.setConsumeTimeout(consumeTimeout);
    }

    /**
     * 是否开启vip通道
     * 
     * @param vipChannelEnabled
     */
    public void setVipChannelEnabled(boolean vipChannelEnabled) {
        consumer.setVipChannelEnabled(vipChannelEnabled);
    }

    public long getRetryMessageResetTo() {
        return retryMessageResetTo;
    }

    public void setRetryMessageResetTo(long retryMessageResetTo) {
        logger.info("topic:{}'s consumer:{} retryMessageReset {}->{}", getTopic(), getGroup(), this.retryMessageResetTo, retryMessageResetTo);
        this.retryMessageResetTo = retryMessageResetTo;
    }
    
    /**
     * 最大重新消费次数
     * 默认为16次
     * @param maxReconsumeTimes
     */
    public void setMaxReconsumeTimes(int maxReconsumeTimes) {
        consumer.setMaxReconsumeTimes(maxReconsumeTimes);
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public void setRateLimiter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /**
     * 设置速率
     * @param permitsPerSecond
     */
    public void setRate(int permitsPerSecond) {
        if (permitsPerSecond < 1) {
            logger.warn("topic:{}'s consumer:{} qps:{} must >= 1", getTopic(), getGroup(), permitsPerSecond);
            return;
        }
        rateLimiter.setRate(permitsPerSecond);
    }
    
    public int getRate() {
        return rateLimiter.getRate();
    }
    
    public void setPause(boolean pause) {
        logger.info("topic:{}'s consumer:{} pause changed: {}->{}", getTopic(), getGroup(), isPause(), pause);
        consumer.getDefaultMQPushConsumerImpl().setPause(pause);
    }

    public boolean isPause() {
        return consumer.getDefaultMQPushConsumerImpl().isPause();
    }

    public void setEnableRateLimit(boolean enableRateLimit) {
        if (rateLimiter instanceof SwitchableRateLimiter) {
            ((SwitchableRateLimiter) rateLimiter).setEnabled(enableRateLimit);
        }
    }
    
    public boolean isEnableRateLimit() {
        if (rateLimiter instanceof SwitchableRateLimiter) {
            return ((SwitchableRateLimiter) rateLimiter).isEnabled();
        }
        return false;
    }
    
    public long getShutdownWaitMaxMillis() {
        return shutdownWaitMaxMillis;
    }

    public void setShutdownWaitMaxMillis(long shutdownWaitMaxMillis) {
        this.shutdownWaitMaxMillis = shutdownWaitMaxMillis;
    }

    /**
     * 初始化漏桶限速器
     */
    public void initLeakyBucketRateLimiter() {
        initRateLimiter(new LeakyBucketRateLimiter(group, 2 * consumer.getConsumeThreadMin(),
                Constant.LIMIT_CONSUME_TPS, TimeUnit.SECONDS));
    }
    
    /**
     * 初始化令牌桶限速器
     */
    public void initTokenBucketRateLimiter() {
        initRateLimiter(new TokenBucketRateLimiter(Constant.LIMIT_CONSUME_TPS));
    }
    
    
    /**
     * 初始化限速器
     * @param rateLimiter
     */
    public void initRateLimiter(RateLimiter rateLimiter) {
        SwitchableRateLimiter switchableRateLimiter = new SwitchableRateLimiter();
        switchableRateLimiter.setName(group);
        switchableRateLimiter.setRateLimiter(rateLimiter);
        this.rateLimiter = switchableRateLimiter;
    }
    
    protected Class<?> getConsumerParameterTypeClass() {
        return consumerParameterTypeClass;
    }

    public void initConsumerParameterTypeClass() {
        consumerParameterTypeClass = _getConsumerParameterTypeClass();
    }
    
    /**
     * 获取消费者参数类型
     * @return
     */
    private Class<?> _getConsumerParameterTypeClass() {
        Method[] methods = getConsumerCallback().getClass().getMethods();
        for (Method method : methods) {
            if (!"call".equals(method.getName())) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (!method.getReturnType().equals(Void.TYPE)) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 2) {
                continue;
            }
            if (MessageExt.class != parameterTypes[1]) {
                continue;
            }
            logger.info("consumer:{}'s parameterTypeClass:{}", getGroup(), parameterTypes[0].getName());
            return parameterTypes[0];
        }
        return null;
    }
}
