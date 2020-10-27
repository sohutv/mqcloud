package com.sohu.tv.mq.cloud.util;

import java.lang.reflect.Field;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.HasSchema;
import io.protostuff.runtime.RuntimeEnv;

/**
 * reference @DefaultIdStrategy
 * 
 * @author yongfeigao
 * @date 2020年10月10日
 */
public class MQCloudIdStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MQCloudIdStrategy.class);

    /**
     * 移除schema
     * 
     * @param className
     * @return
     */
    @SuppressWarnings("unchecked")
    public static HasSchema<?> removeSchema(String className) {
        try {
            Field field = DefaultIdStrategy.class.getDeclaredField("pojoMapping");
            field.setAccessible(true);
            Map<String, HasSchema<?>> pojoMapping = (Map<String, HasSchema<?>>) field.get(RuntimeEnv.ID_STRATEGY);
            return pojoMapping.remove(className);
        } catch (Exception e) {
            logger.error("removeSchema:{}", className, e);
        }
        return null;
    }
}
