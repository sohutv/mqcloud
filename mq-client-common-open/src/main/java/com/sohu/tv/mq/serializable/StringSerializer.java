package com.sohu.tv.mq.serializable;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * String 序列化
 * 
 * @author yongfeigao
 * @date 2019年2月26日
 * @param <T>
 */
public class StringSerializer<T> implements MessageSerializer<T> {

    @Override
    public byte[] serialize(T source) throws Exception {
        if (!(source instanceof String)) {
            throw new NotImplementedException(
                    "please publish(String) or use DefaultMessageSerializer for " + source.getClass());
        }
        return ((String) source).getBytes(RemotingHelper.DEFAULT_CHARSET);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(byte[] bytes) throws Exception {
        return (T) new String(bytes, RemotingHelper.DEFAULT_CHARSET);
    }

}
