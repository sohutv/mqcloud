package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;
import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.bo.ConsumerStat;

/**
 * 消费状态监控
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年11月14日
 */
public class ConsumerMonitorVO {
    // 消费者状态集合
    private List<ConsumerStat> consumerStat;
    // 默认的报警配置
    private AlarmConfig defaultConfig;
    // 用户自定义的报警配置
    private List<AlarmConfig> alarmConfig;

    public List<ConsumerStat> getConsumerStat() {
        return consumerStat;
    }

    public void setConsumerStat(List<ConsumerStat> consumerStat) {
        this.consumerStat = consumerStat;
    }

    public AlarmConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(AlarmConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public List<AlarmConfig> getAlarmConfig() {
        return alarmConfig;
    }

    public void setAlarmConfig(List<AlarmConfig> alarmConfig) {
        this.alarmConfig = alarmConfig;
    }
}
