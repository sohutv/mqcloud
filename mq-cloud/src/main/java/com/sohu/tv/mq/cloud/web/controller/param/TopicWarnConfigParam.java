package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.Range;

/**
 * topic预警配置参数
 *
 * @author yongfeigao
 * @date 2024年09月10日
 */
public class TopicWarnConfigParam {
    private Long id;
    private long tid;
    // 操作数类型
    @Range(min = 0, max = 5)
    private int operandType;
    // 操作符类型
    @Range(min = 0, max = 3)
    private int operatorType;
    // 阈值
    private double threshold;
    // 报警间隔，单位分钟
    @Range(min = 0)
    private int warnInterval;
    // 报警配置生效开始时间
    private String warnConfigTimeStart;
    // 报警配置生效结束时间
    private String warnConfigTimeEnd;

    // 是否启用 0:不启用 1:启用
    @Range(min = 0, max = 1)
    private int enabled;

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public int getOperandType() {
        return operandType;
    }

    public void setOperandType(int operandType) {
        this.operandType = operandType;
    }

    public int getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(int operatorType) {
        this.operatorType = operatorType;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getWarnInterval() {
        return warnInterval;
    }

    public void setWarnInterval(int warnInterval) {
        this.warnInterval = warnInterval;
    }

    public String getWarnTime() {
        if (warnConfigTimeStart == null || warnConfigTimeEnd == null) {
            return null;
        }
        return warnConfigTimeStart + "-" + warnConfigTimeEnd;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public String getWarnConfigTimeStart() {
        return warnConfigTimeStart;
    }

    public void setWarnConfigTimeStart(String warnConfigTimeStart) {
        this.warnConfigTimeStart = warnConfigTimeStart;
    }

    public String getWarnConfigTimeEnd() {
        return warnConfigTimeEnd;
    }

    public void setWarnConfigTimeEnd(String warnConfigTimeEnd) {
        this.warnConfigTimeEnd = warnConfigTimeEnd;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TopicWarnConfigParam{" +
                "id=" + id +
                ", tid=" + tid +
                ", operandType=" + operandType +
                ", operatorType=" + operatorType +
                ", threshold=" + threshold +
                ", warnInterval=" + warnInterval +
                ", warnConfigTimeStart='" + warnConfigTimeStart + '\'' +
                ", warnConfigTimeEnd='" + warnConfigTimeEnd + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
