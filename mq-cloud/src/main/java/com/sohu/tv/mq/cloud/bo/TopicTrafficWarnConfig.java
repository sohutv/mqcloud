package com.sohu.tv.mq.cloud.bo;

/**
 * @author yongweizhao
 * @create 2020/9/17 17:33
 */
public class TopicTrafficWarnConfig {
    // 平均值乘数因子
    private Float avgMultiplier;
    // 最大值平均值增幅百分比
    private Float avgMaxPercentageIncrease;
    // 最大值增幅百分比
    private Float maxMaxPercentageIncrease;
    // 告警接收人,0:生产者消费者及管理员,1:生产者和管理员,2:消费者和管理员,3:仅管理员,4:不告警
    private Integer alarmReceiver;
    // topic名称
    private String topic;

    public TopicTrafficWarnConfig() {}

    public void copyProperties(TopicTrafficWarnConfig defaultConfig) {
        if (this.avgMultiplier == null) {
            this.avgMultiplier = defaultConfig.getAvgMultiplier();
        }
        if (this.avgMaxPercentageIncrease == null) {
            this.avgMaxPercentageIncrease = defaultConfig.getAvgMaxPercentageIncrease();
        }
        if (this.maxMaxPercentageIncrease == null) {
            this.maxMaxPercentageIncrease = defaultConfig.getMaxMaxPercentageIncrease();
        }
        if (this.alarmReceiver == null) {
            this.alarmReceiver = defaultConfig.getAlarmReceiver();
        }
    }
    /**
     * 是否接收报警
     * @return
     */
    public boolean isAlert() {
        return this.alarmReceiver != 4;
    }

    public Float getAvgMultiplier() {
        return avgMultiplier;
    }

    public void setAvgMultiplier(Float avgMultiplier) {
        this.avgMultiplier = avgMultiplier;
    }

    public Float getAvgMaxPercentageIncrease() {
        return avgMaxPercentageIncrease;
    }

    public void setAvgMaxPercentageIncrease(Float avgMaxPercentageIncrease) {
        this.avgMaxPercentageIncrease = avgMaxPercentageIncrease;
    }

    public Float getMaxMaxPercentageIncrease() {
        return maxMaxPercentageIncrease;
    }

    public void setMaxMaxPercentageIncrease(Float maxMaxPercentageIncrease) {
        this.maxMaxPercentageIncrease = maxMaxPercentageIncrease;
    }

    public Integer getAlarmReceiver() {
        return alarmReceiver;
    }

    public void setAlarmReceiver(Integer alarmReceiver) {
        this.alarmReceiver = alarmReceiver;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
