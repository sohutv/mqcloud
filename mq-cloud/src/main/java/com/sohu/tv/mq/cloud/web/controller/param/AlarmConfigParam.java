package com.sohu.tv.mq.cloud.web.controller.param;

import com.sohu.tv.mq.cloud.util.Result;
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
    @Range(min = 0, max = 604800)
    private Long accumulateTime;
    // 堆积数量
    @Range(min = 1, max = 10000000000L)
    private Long accumulateCount;
    // 阻塞时间
    @Range(min = 0, max = 604800)
    private Long blockTime;
    // 消费失败数量
    @Range(min = 0, max = 10000000000L)
    private Long consumerFailCount;
    // 死消息数量
    @Range(min = 0, max = 10000000000L)
    private Long consumerDeadCount;
    // 单位时间，超过单位时间内的次数则不报警
    @Range(min = 1, max = 72)
    private Integer warnUnitTime;
    // 单位时间内的次数
    @Range(min = 1, max = 100)
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

    public Long getConsumerDeadCount() {
        return consumerDeadCount;
    }

    public void setConsumerDeadCount(Long consumerDeadCount) {
        this.consumerDeadCount = consumerDeadCount;
    }

    public Result<?> validAndResetProperties() {
        if (accumulateTime != null) {
            if (accumulateTime < 0) {
                return Result.getErrorResult("堆积时间不能为负数");
            }
            accumulateTime *= 1000;
        }
        if (accumulateCount != null) {
            if (accumulateCount < 0) {
                return Result.getErrorResult("堆积数量不能为负数");
            }
        }
        if (blockTime != null) {
            if (blockTime < 0) {
                return Result.getErrorResult("阻塞时间不能为负数");
            }
            blockTime *= 1000;
        }
        if (consumerFailCount != null) {
            if (consumerFailCount < 0) {
                return Result.getErrorResult("消费失败数量不能为负数");
            }
        }
        if (consumerDeadCount != null) {
            if (consumerDeadCount < 0) {
                return Result.getErrorResult("死消息数量不能为负数");
            }
        }
        if (warnUnitTime != null) {
            if (warnUnitTime <= 0) {
                return Result.getErrorResult("单位时间必须大于0");
            }
        }
        if (warnUnitCount != null) {
            if (warnUnitCount <= 0) {
                return Result.getErrorResult("单位时间内的次数必须大于0");
            }
        }
        return Result.getOKResult();
    }

    @Override
    public String toString() {
        return "AlarmConfigParam{" +
                "consumer='" + consumer + '\'' +
                ", accumulateTime=" + accumulateTime +
                ", accumulateCount=" + accumulateCount +
                ", blockTime=" + blockTime +
                ", consumerFailCount=" + consumerFailCount +
                ", consumerDeadCount=" + consumerDeadCount +
                ", warnUnitTime=" + warnUnitTime +
                ", warnUnitCount=" + warnUnitCount +
                ", ignoreWarn=" + ignoreWarn +
                '}';
    }
}
