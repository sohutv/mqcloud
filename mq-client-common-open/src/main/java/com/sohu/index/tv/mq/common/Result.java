package com.sohu.index.tv.mq.common;

import java.io.Serializable;

/**
 * Created by yijunzhang on 14-7-28.
 */
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 5760423804093292846L;

    /**
     * 返回状态，是否成功
     */
    public boolean isSuccess;

    /**
     * 返回结果
     */
    private T result;

    /**
     * 异常信息
     */
    private Throwable exception;
    
    // 正在重试
    private boolean retrying;
    
    // 重试过的次数
    private int retriedTimes;
    
    private MQMessage<?> mqMessage;
    
    public Result(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Result(boolean isSuccess, T result) {
        this.isSuccess = isSuccess;
        this.result = result;
    }

    public Result(boolean isSuccess, Throwable exception) {
        this.isSuccess = isSuccess;
        this.exception = exception;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Exception getException() {
        return (Exception) exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public boolean isRetrying() {
        return retrying;
    }

    public Result<T> setRetrying(boolean retrying) {
        this.retrying = retrying;
        return this;
    }

    public int getRetriedTimes() {
        return retriedTimes;
    }

    public void setRetriedTimes(int retriedTimes) {
        this.retriedTimes = retriedTimes;
    }

    @SuppressWarnings("unchecked")
    public <R> MQMessage<R> getMqMessage() {
        return (MQMessage<R>) mqMessage;
    }

    public void setMqMessage(MQMessage<?> mqMessage) {
        this.mqMessage = mqMessage;
    }

    @Override
    public String toString() {
        return "Result [isSuccess=" + isSuccess + ", result=" + result + ", exception=" + exception + ", retrying="
                + retrying + ", retriedTimes=" + retriedTimes + "]";
    }
}
