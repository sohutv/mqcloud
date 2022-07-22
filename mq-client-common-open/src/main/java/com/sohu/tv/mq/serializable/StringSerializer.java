package com.sohu.tv.mq.serializable;

import com.sohu.tv.mq.util.JSONUtil;
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
        // 兼容非String的类型
        if (!(source instanceof String)) {
            source = (T) JSONUtil.toJSONString(source);
        }
        return ((String) source).getBytes(RemotingHelper.DEFAULT_CHARSET);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(byte[] bytes) throws Exception {
        return (T) new String(bytes, RemotingHelper.DEFAULT_CHARSET);
    }

}
