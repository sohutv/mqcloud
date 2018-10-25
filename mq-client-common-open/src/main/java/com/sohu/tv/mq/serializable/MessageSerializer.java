package com.sohu.tv.mq.serializable;

/**
 * 消息序列化
 * 
 * @author yongfeigao
 * @date 2018年10月18日
 * @param <T>
 */
public interface MessageSerializer<T> {
    
    /**
     * 序列化
     * 
     * @param source
     * @return
     */
    public byte[] serialize(T source);

    /**
     * 反序列化
     * 
     * @param bytes
     * @return
     */
    public T deserialize(byte[] bytes);
}
