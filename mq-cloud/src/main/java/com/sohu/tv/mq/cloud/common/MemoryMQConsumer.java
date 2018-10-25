package com.sohu.tv.mq.cloud.common;

/**
 * MemoryMQ消费者
 * @Description: 
 * @author yongfeigao
 * @date 2018年3月5日
 * @param <T>
 */
public interface MemoryMQConsumer<T> {
    /**
     * 消费数据
     * @param t
     * @throws Exception
     */
    public void consume(T t) throws Exception;
}
