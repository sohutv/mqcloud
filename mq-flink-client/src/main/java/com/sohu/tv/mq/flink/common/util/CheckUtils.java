package com.sohu.tv.mq.flink.common.util;

import com.sohu.tv.mq.flink.common.config.FlinkSourceConsumerConfig;
import com.sohu.tv.mq.flink.common.config.FlinkSinkProducerConfig;
import org.apache.commons.lang3.Validate;

/**
 * @project mqcloud
 * @description
 * @author fengwang219475
 * @date 2024/5/15 16:17:45
 * @version 1.0
 */
public class CheckUtils {
    
    public static void checkConsumerConfig(FlinkSourceConsumerConfig consumerConfig) {
        Validate.notNull(consumerConfig, "consumerConfig can not be null");
        Validate.notNull(consumerConfig.getTopic(), "topic can not be null");
        Validate.notNull(consumerConfig.getConsumerGroup(), "consumerGroup can not be null");
        Validate.notNull(consumerConfig.getMqCloudDomain(), "mqCloudDomain can not be null");
    }

    public static void checkProducerConfig(FlinkSinkProducerConfig producerConfig) {
        Validate.notNull(producerConfig, "producerConfig can not be null");
        Validate.notNull(producerConfig.getTopic(), "topic can not be null");
        Validate.notNull(producerConfig.getProducerGroup(), "producerGroup can not be null");
        Validate.notNull(producerConfig.getMqCloudDomain(), "mqCloudDomain can not be null");
    }
}
