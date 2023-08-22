package com.sohu.tv.mq.dto;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description web接口结果
 * @date 2023/7/10 20:12:05
 */
public class WebResult<T> extends AbstractResult {

    private T result;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public static WebResult setFail(int status, String message) {
        WebResult webResult = new WebResult();
        webResult.setStatus(status);
        webResult.setMessage(message);
        return webResult;
    }
}
