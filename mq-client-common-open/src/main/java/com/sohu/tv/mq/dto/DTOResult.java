package com.sohu.tv.mq.dto;
/**
 * dto结果
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月3日
 */
public class DTOResult<T> extends AbstractResult {
    private T result;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
