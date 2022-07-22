package com.sohu.tv.mq.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.util.List;

/**
 * json工具
 *
 * @author: yongfeigao
 * @date: 2022/7/1 16:28
 */
public class JSONUtil {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        // 属性值为null，不序列化
        mapper.setSerializationInclusion(Include.NON_NULL);
        // 反序列化时，对象中缺少相应的属性，不会报错
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 忽略 transient 修饰的属性
        mapper.enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
        // 空对象时禁止抛出异常
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * 将对象转换为json string
     *
     * @param entity
     * @return
     */
    public static String toJSONString(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(entity);
        } catch (IOException e) {
            throw new JSONExecption(e);
        }
    }

    /**
     * 从json解析出对象
     *
     * @param <T>
     * @param content
     * @param valueType
     * @return
     */
    public static <T> T parse(String content, Class<T> valueType) {
        if (content == null) {
            return null;
        }
        try {
            return mapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new JSONExecption(e);
        }
    }

    /**
     * 从json解析出对象
     *
     * @param <T>
     * @param content
     * @return
     */
    public static <T> T parseList(String content, Class<?> typeClass) {
        if (content == null) {
            return null;
        }
        try {
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, typeClass);
            return mapper.readValue(content, type);
        } catch (IOException e) {
            throw new JSONExecption(e);
        }
    }

    /**
     * 从json解析出对象
     *
     * @param content
     * @param typeClass
     * @param paramClass
     * @param <T>
     * @return
     */
    public static <T> T parse(String content, Class<?> typeClass, Class<?> paramClass) {
        if (content == null) {
            return null;
        }
        try {
            JavaType type = mapper.getTypeFactory().constructParametricType(typeClass, paramClass);
            return mapper.readValue(content, type);
        } catch (IOException e) {
            throw new JSONExecption(e);
        }
    }

    /**
     * json异常
     */
    public static class JSONExecption extends RuntimeException {
        public JSONExecption() {
            super();
        }

        public JSONExecption(String message) {
            super(message);
        }

        public JSONExecption(String message, Throwable cause) {
            super(message, cause);
        }

        public JSONExecption(Throwable cause) {
            super(cause);
        }

        protected JSONExecption(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}