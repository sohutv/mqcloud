package com.sohu.tv.mq.cloud.util;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;

/**
 * 消费预警配置类型
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年11月15日
 */
public enum ConsumerWarnEnum {

    ACCUMULATE_COUNT(1, 10000L), 
    ACCUMULATE_TIME(2, 300000L), 
    BLOCK_TIME(3, 10000L), 
    CONSUMER_FAIL_COUNT(4, 10L), 
    WARN_UNIT_TIME(5, 1L), 
    WARN_UNIT_COUNT(6, 1L),
    ;

    private int id;
    // 此项为对应配置的默认值
    private Long value;

    private ConsumerWarnEnum(int id, Long value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public Long getValue() {
        return value;
    }

    /**
     * 根据不同的type返回不同的预警值
     * 
     * @param config
     * @param type
     * @return
     */
    public static Long getRealValue(AlarmConfig config, ConsumerWarnEnum consumerWarnEnum) {
        if (!config.isAlert()) {
            return (long) -1;
        }
        switch (consumerWarnEnum) {
            case ACCUMULATE_COUNT:
                return config.getAccumulateCount();
            case ACCUMULATE_TIME:
                return config.getAccumulateTime();
            case BLOCK_TIME:
                return config.getBlockTime();
            case CONSUMER_FAIL_COUNT:
                return config.getConsumerFailCount();
            case WARN_UNIT_TIME:
                return (long) config.getWarnUnitTime();
            case WARN_UNIT_COUNT:
                return (long) config.getWarnUnitCount();
            default:
                break;
        }
        return null;
    }

}
