package com.sohu.tv.mq.common;

import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.util.LogUtil;

/**
 * 抽象隔离
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年1月24日
 * @param <T>
 */
public abstract class AbstractCommand<T> extends HystrixCommand<T> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int POOLSIZE = 30;
    public static final String GROUP_SUFFIX = "group";
    public static final String COMMAND_SUFFIX = "cmd";
    // 警报器
    private Alerter alerter;

    /**
     * @param groupKey
     * @param commandKey
     * @param timeout 超时时间
     */
    @SuppressWarnings("deprecation")
    public AbstractCommand(String groupKey, String commandKey, int poolSize, int timeout, Alerter alerter) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey + "_" + GROUP_SUFFIX))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey + "_" + COMMAND_SUFFIX))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter().withExecutionIsolationThreadTimeoutInMilliseconds(timeout))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(poolSize)));
        this.alerter = alerter;
    }

    protected T run() throws Exception {
        try {
            return invoke();
        } catch (Exception e) {
            logger.error("send err! "+getCommandGroup().name() + "-" +
                    getCommandKey().name() + ":" +
                    invokeErrorInfo(),
                    e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 子类实现该方法完成具体的业务逻辑
     * 
     * @return
     * @throws Exception
     */
    protected abstract T invoke() throws Exception;

    /**
     * 子类实现该方法完成业务逻辑抛出异常时,进行日志记录,或处理等
     * 
     * @param e
     */
    protected abstract Object invokeErrorInfo();

    /**
     * 降级方法，判断熔断器是否打开，打开的话进行预警
     */
    public T getFallback() {
        // 判断熔断器是否打开
        if (super.isCircuitBreakerOpen()) {
            if (null != alerter) {
                String info = "group:" + getCommandGroup().name() + " command:" + getCommandKey().name() + " err!";
                alerter.alert(info);
            }
        }
        return fallback();
    }

    /**
     * 子类实现该方法完成降级处理
     * 
     * @return
     */
    public abstract T fallback();

    /**
     * 提供记录日志的方法
     * @param logger
     * @param info
     * @return
     */
    @SuppressWarnings("unchecked")
    public T execute(Logger logger) {
        T t = super.execute();
        try {
            LogUtil.log(logger, (Result<SendResult>) t, invokeErrorInfo());
        } catch (Exception e) {
            logger.warn("log err:{}", e.getMessage());
        }
        return t;
    }
}
