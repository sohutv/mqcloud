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
public interface BatchConsumerCallback<T> {

    /**
     * 订阅回调方法
     *
     * @return
     */
    void call(List<MQMessage<T>> batchMessage) throws Exception;
}
