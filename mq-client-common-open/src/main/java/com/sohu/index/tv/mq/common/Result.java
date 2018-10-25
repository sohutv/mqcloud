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
    private Exception exception;
    
    public Result(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Result(boolean isSuccess, T result) {
        this.isSuccess = isSuccess;
        this.result = result;
    }

    public Result(boolean isSuccess, Exception exception) {
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
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
