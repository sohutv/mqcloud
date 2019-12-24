package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.Range;

/**
 * 预警配置项
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月28日
 */
public class AlarmConfigParam {
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
    // 单位时间，超过单位时间内的次数则不报警
    private Integer warnUnitTime;
    // 单位时间内的次数
    private Integer warnUnitCount;
    // 报警总开关，是否接收报警
    @Range(min = 0, max = 1)
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
}
