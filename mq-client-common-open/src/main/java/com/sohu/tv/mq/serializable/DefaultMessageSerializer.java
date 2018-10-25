package com.sohu.tv.mq.serializable;


import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * 序列化工具
 * 
 * @Description: protostuff 提供序列化
 * @author copy from indexmq
 * @date 2018年1月17日
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultMessageSerializer<T> implements MessageSerializer<T> {
    /**
     * 序列化
     * 
     * @param source
     * @return
     */
    public byte[] serialize(T source) {
        VO<T> vo = new VO<T>(source);
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            final Schema<VO> schema = RuntimeSchema.getSchema(VO.class);
            return ProtostuffIOUtil.toByteArray(vo, schema, buffer);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 反序列化
     * 
     * @param bytes
     * @return
     */
    public T deserialize(final byte[] bytes) {
        try {
            Schema<VO> schema = RuntimeSchema.getSchema(VO.class);
            VO vo = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(bytes, vo, schema);
            if (vo != null && vo.getValue() != null) {
                return (T) vo.getValue();
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }
}
