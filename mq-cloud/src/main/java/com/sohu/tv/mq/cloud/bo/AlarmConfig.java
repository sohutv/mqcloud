package com.sohu.tv.mq.cloud.bo;

/**
 * 预警配置项
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月26日
 */
public class AlarmConfig {
    // 报警
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
    // 单位时间，超过单位时间内的次数则不报警,单位小时
    private Integer warnUnitTime;
    // 单位时间内的次数
    private Integer warnUnitCount;
    // 报警总开关，是否接收报警
    private Integer ignoreWarn;

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
        return ignoreWarn;
    }

    public void setIgnoreWarn(Integer ignoreWarn) {
        this.ignoreWarn = ignoreWarn;
    }

    // 为0的配置项，页面显示空
    public String getAccumulateTimeShow() {
        return showValue(accumulateTime);
    }

    public String getAccumulateCountShow() {
        return showValue(accumulateCount);
    }

    public String getBlockTimeShow() {
        return showValue(blockTime);
    }

    public String getConsumerFailCountShow() {
        return showValue(consumerFailCount);
    }

    /**
     * 是否接收报警
     * 
     * @return
     */
    public boolean isAlert() {
        return ignoreWarn == ALERT;
    }

    /**
     * 拼接报警频率
     * 
     * @return
     */
    public String spliceWarnFrequency() {
        if (warnUnitTime == null || warnUnitCount == null) {
            return null;
        }
        return warnUnitTime + "小时" + warnUnitCount + "次";
    }

    /**
     * 为null不显示
     * 
     * @param arg
     * @return
     */
    private String showValue(Long arg) {
        return arg == null ? "" : String.valueOf(arg);
    }
}
