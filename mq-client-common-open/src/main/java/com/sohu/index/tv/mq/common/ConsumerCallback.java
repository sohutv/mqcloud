package com.sohu.index.tv.mq.common;

/**
 * 消费回调
 * @Description: 
 * @author yongfeigao
 * @date 2018年1月19日
 * @param <T> msg obj
 * @param MessageExt
 */
public interface ConsumerCallback<T, MessageExt> {

    /**
     * 订阅回调方法
     *
     * @return
     */
    void call(T t, MessageExt k) throws Exception;

}
