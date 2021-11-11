package com.sohu.index.tv.mq.common;

import java.util.List;

/**
 * 批量消费回调
 * @Description: 
 * @author yongfeigao
 * @date 2018年1月19日
 * @param <T> msg obj
 * @param MessageExt
 */
public interface BatchConsumerCallback<T, C> {

    /**
     * 订阅回调方法
     * @param batchMessage
     * @param context @ConsumeConcurrentlyContext or @ConsumeOrderlyContext
     * @throws Exception
     */
    void call(List<MQMessage<T>> batchMessage, C context) throws Exception;
}
