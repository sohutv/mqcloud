package com.sohu.tv.mq.cloud.bo;

/**
 * 预警配置项
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月26日
 */
public class AlarmConfig {
    // 预警
    public static int ALERT = 0;

    // consumer名称，为空行为默认配置
    private String consumer;
    // 堆积时间
    private Long accumulateTime;
    // 堆积数量
    private Long accumulateCount;
    // 阻塞时间
    private Long blockTime;
    // 消费失败数量
    private Long consumerFailCount;
    // 单位时间，超过单位时间内的次数则不预警,单位小时
    private Integer warnUnitTime;
    // 单位时间内的次数
    private Integer warnUnitCount;
    // 预警总开关，是否接收预警
    private Integer ignoreWarn;
    // 消费死消息数量
    private Long consumerDeadCount;

    private AlarmConfig defaultConfig;

    public AlarmConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(AlarmConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public Long getAccumulateTime() {
        return accumulateTime;
    }

    public void setAccumulateTime(Long accumulateTime) {
        this.accumulateTime = accumulateTime;
    }

    public Long getAccumulateCount() {
        return accumulateCount;
    }

    public void setAccumulateCount(Long accumulateCount) {
        this.accumulateCount = accumulateCount;
    }

    public Long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Long blockTime) {
        this.blockTime = blockTime;
    }

    public Long getConsumerFailCount() {
        return consumerFailCount;
    }

    public void setConsumerFailCount(Long consumerFailCount) {
        this.consumerFailCount = consumerFailCount;
    }

    public Long getConsumerDeadCount() {
        return consumerDeadCount;
    }

    public void setConsumerDeadCount(Long consumerDeadCount) {
        this.consumerDeadCount = consumerDeadCount;
    }

    public Integer getWarnUnitTime() {
        return warnUnitTime;
    }

    public void setWarnUnitTime(Integer warnUnitTime) {
        this.warnUnitTime = warnUnitTime;
    }

    public Integer getWarnUnitCount() {
        return warnUnitCount;
    }

    public void setWarnUnitCount(Integer warnUnitCount) {
        this.warnUnitCount = warnUnitCount;
    }

    public Integer getIgnoreWarn() {
        if (ignoreWarn != null) {
            return ignoreWarn;
        }
        return defaultConfig.getIgnoreWarn();
    }

    public void setIgnoreWarn(Integer ignoreWarn) {
        this.ignoreWarn = ignoreWarn;
    }

    public String getAccumulateTimeShow() {
        return showValue(accumulateTime == null ? defaultConfig.accumulateTime : accumulateTime, 1000, "秒");
    }

    public Long getAccumulateTimeValue() {
        return accumulateTime == null ? defaultConfig.accumulateTime / 1000 : accumulateTime / 1000;
    }

    public Long getAccumulateCountValue() {
        return accumulateCount == null ? defaultConfig.accumulateCount : accumulateCount;
    }

    public String getBlockTimeShow() {
        return showValue(blockTime == null ? defaultConfig.blockTime : blockTime, 1000, "秒");
    }

    public Long getBlockTimeValue() {
        return blockTime == null ? defaultConfig.blockTime / 1000 : blockTime / 1000;
    }

    public String getConsumerFailCountShow() {
        return showValue(consumerFailCount == null ? defaultConfig.consumerFailCount : consumerFailCount);
    }

    public String getConsumerDeadCountShow() {
        return showValue(consumerDeadCount == null ? defaultConfig.consumerDeadCount : consumerDeadCount);
    }

    public String getWarnUnitTimeShow() {
        return showValue(Long.valueOf(warnUnitTime == null ? defaultConfig.warnUnitTime : warnUnitTime), "小时");
    }

    public Integer getWarnUnitTimeValue() {
        return warnUnitTime == null ? defaultConfig.warnUnitTime : warnUnitTime;
    }

    public Integer getWarnUnitCountValue() {
        return warnUnitCount == null ? defaultConfig.warnUnitCount : warnUnitCount;
    }

    public Integer getIgnoreWarnValue() {
        return ignoreWarn == null ? defaultConfig.ignoreWarn : ignoreWarn;
    }

    /**
     * 是否接收预警
     * 
     * @return
     */
    public boolean isAlert() {
        return ignoreWarn == ALERT;
    }

    private String showValue(Long arg) {
        return showValue(arg, 0, null);
    }

    private String showValue(Long arg, String suffix) {
        return showValue(arg, 0, suffix);
    }

    /**
     * 为null不显示
     */
    private String showValue(Long arg, long unit, String suffix) {
        if (arg == null) {
            return "";
        }
        String value = unit == 0 ? String.valueOf(arg) : String.valueOf(arg / unit);
        return value + (suffix == null ? "" : suffix);
    }

    public void initDefaultConfigValue() {
        defaultConfig = new AlarmConfig();
        defaultConfig.setAccumulateTime(300000L);
        defaultConfig.setAccumulateCount(10000L);
        defaultConfig.setBlockTime(10000L);
        defaultConfig.setConsumerFailCount(10L);
        defaultConfig.setWarnUnitTime(1);
        defaultConfig.setWarnUnitCount(1);
        defaultConfig.setIgnoreWarn(0);
        defaultConfig.setConsumerDeadCount(0L);
    }
}
