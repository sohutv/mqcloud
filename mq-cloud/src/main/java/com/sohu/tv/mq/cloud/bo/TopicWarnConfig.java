package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.service.AlarmConfigBridingService;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * topic预警配置
 *
 * @author yongfeigao
 * @date 2024年09月06日
 */
public class TopicWarnConfig {
    private Long id;
    private long tid;
    // 操作数类型
    private int operandType;
    // 操作符类型
    private int operatorType;
    // 阈值
    private double threshold;
    // 报警间隔，单位分钟
    private int warnInterval;
    // 报警时间
    private String warnTime;
    // 是否启用 0:不启用 1:启用
    private int enabled;

    // 冗余字段
    private double actualValue;
    private String remark;

    public TopicWarnConfig() {
    }

    public TopicWarnConfig(TopicWarnConfig topicWarnConfig) {
        this.operandType = topicWarnConfig.operandType;
        this.operatorType = topicWarnConfig.operatorType;
        this.threshold = topicWarnConfig.threshold;
        this.warnInterval = topicWarnConfig.warnInterval;
        this.warnTime = topicWarnConfig.warnTime;
    }

    /**
     * 操作数类型
     */
    public enum OperandType {
        TRAFFIC_5_MIN(0, "每5分钟的消息量"),
        TRAFFIC_1_HOUR(1, "每小时的消息量"),
        TRAFFIC_1_DAY(2, "每天的消息量"),
        TRAFFIC_MINUTE5_TO_MINUTE5(3, "消息量按5分钟环比"),
        TRAFFIC_HOUR_TO_HOUR(4, "消息量按小时环比"),
        TRAFFIC_DAY_TO_DAY(5, "消息量按天环比"),
        ;

        private int type;
        private String desc;

        public static List<Integer> MINUTE_5_LIST = Arrays.asList(TRAFFIC_5_MIN.getType(), TRAFFIC_MINUTE5_TO_MINUTE5.type);
        public static List<Integer> HOUR_LIST = Arrays.asList(TRAFFIC_1_HOUR.getType(), TRAFFIC_HOUR_TO_HOUR.type);
        public static List<Integer> DAY_LIST = Arrays.asList(TRAFFIC_1_DAY.getType(), TRAFFIC_DAY_TO_DAY.type);

        OperandType(int type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public static OperandType getByType(int type) {
            for (OperandType operandType : OperandType.values()) {
                if (operandType.type == type) {
                    return operandType;
                }
            }
            return null;
        }

        public boolean isPercentType() {
            return this == TRAFFIC_MINUTE5_TO_MINUTE5 || this == TRAFFIC_HOUR_TO_HOUR || this == TRAFFIC_DAY_TO_DAY;
        }

        public int getType() {
            return type;
        }
    }

    /**
     * 操作符类型
     */
    public enum OperatorType {
        GREATER_THAN(0, "大于"),
        LESS_THAN(1, "小于"),
        GREATER_THAN_OR_EQUAL(2, "大于等于"),
        LESS_THAN_OR_EQUAL(3, "小于等于"),
        ;

        private int type;
        private String desc;

        OperatorType(int type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public boolean isTrue(double operand1, double operand2) {
            switch (this) {
                case GREATER_THAN:
                    return operand1 > operand2;
                case LESS_THAN:
                    return operand1 < operand2;
                case GREATER_THAN_OR_EQUAL:
                    return operand1 >= operand2;
                case LESS_THAN_OR_EQUAL:
                    return operand1 <= operand2;
            }
            return false;
        }

        public static OperatorType getByType(int type) {
            for (OperatorType operatorType : OperatorType.values()) {
                if (operatorType.type == type) {
                    return operatorType;
                }
            }
            return null;
        }

        public int getType() {
            return type;
        }
    }

    public TopicWarnConfig copy() {
        TopicWarnConfig topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setTid(tid);
        topicWarnConfig.setOperandType(operandType);
        topicWarnConfig.setOperatorType(operatorType);
        topicWarnConfig.setThreshold(threshold);
        topicWarnConfig.setWarnInterval(warnInterval);
        topicWarnConfig.setWarnTime(warnTime);
        return topicWarnConfig;
    }

    public TopicWarnConfig warn(double actualValue1, double actualValue2) {
        if (!needWarn(actualValue1)) {
            return null;
        }
        if (!isIntervalValid()) {
            return null;
        }
        TopicWarnConfig clone = copy();
        clone.setActualValue(actualValue1);
        return clone;
    }

    protected boolean isIntervalValid() {
        return true;
    }

    /**
     * 预警策略
     */
    public static class WarnConfigStrategy extends TopicWarnConfig {
        protected String topicName;
        protected AlarmConfigBridingService alarmConfigBridingService;

        public WarnConfigStrategy(TopicWarnConfig topicWarnConfig) {
            super(topicWarnConfig);
        }

        /**
         * 预警频率检测
         */
        @Override
        protected boolean isIntervalValid() {
            return alarmConfigBridingService.needWarn(getWarnIntervalInMillis(), "tw", topicName, String.valueOf(getOperandType()));
        }
    }

    /**
     * 按分钟预警
     */
    public static class MinuteWarnConfig extends WarnConfigStrategy {
        public MinuteWarnConfig(TopicWarnConfig topicWarnConfig) {
            super(topicWarnConfig);
        }
    }

    /**
     * 按小时预警
     */
    public static class HourWarnConfig extends WarnConfigStrategy {
        public HourWarnConfig(TopicWarnConfig topicWarnConfig) {
            super(topicWarnConfig);
        }
    }

    /**
     * 按天预警
     */
    public static class DayWarnConfig extends WarnConfigStrategy {
        public DayWarnConfig(TopicWarnConfig topicWarnConfig) {
            super(topicWarnConfig);
        }
    }

    /**
     * 百分比预警
     */
    public static class PercentWarnConfig extends WarnConfigStrategy {
        public PercentWarnConfig(TopicWarnConfig topicWarnConfig) {
            super(topicWarnConfig);
        }

        @Override
        public TopicWarnConfig warn(double actualValue1, double actualValue2) {
            double actualValue = (actualValue1 - actualValue2) * 100D / actualValue2;
            return super.warn(actualValue, 0);
        }
    }

    public static class Minute5ToMinute5WarnConfig extends PercentWarnConfig {
        public Minute5ToMinute5WarnConfig(TopicWarnConfig topicWarnConfig) {
            super(topicWarnConfig);
        }

        @Override
        public TopicWarnConfig warn(double actualValue1, double actualValue2) {
            TopicWarnConfig result = super.warn(actualValue1, actualValue2);
            if (result != null) {
                result.setRemark("最近5分钟:" + numberFormat(actualValue1) + "条,上一个5分钟:" + numberFormat(actualValue2) + "条");
            }
            return result;
        }
    }

    /**
     * 按小时环比
     */
    public static class HourToHourWarnConfig extends PercentWarnConfig {
        public HourToHourWarnConfig(TopicWarnConfig topicWarnConfig) {
            super(topicWarnConfig);
        }

        @Override
        public TopicWarnConfig warn(double actualValue1, double actualValue2) {
            TopicWarnConfig result = super.warn(actualValue1, actualValue2);
            if (result != null) {
                result.setRemark("前1小时:" + numberFormat(actualValue1) + "条,前2小时:" + numberFormat(actualValue2)+"条");
            }
            return result;
        }
    }

    /**
     * 按天环比
     */
    public static class DayToDayWarnConfig extends PercentWarnConfig {
        public DayToDayWarnConfig(TopicWarnConfig topicWarnConfig) {
            super(topicWarnConfig);
        }

        @Override
        public TopicWarnConfig warn(double actualValue1, double actualValue2) {
            TopicWarnConfig result = super.warn(actualValue1, actualValue2);
            if (result != null) {
                result.setRemark("昨天:" + numberFormat(actualValue1) + "条,前天:" + numberFormat(actualValue2) + "条");
            }
            return result;
        }
    }

    public boolean isInWarnTime() {
        if (warnTime == null) {
            return true;
        }
        String[] times = warnTime.split("-");
        LocalTime start = toLocalTime(times[0]);
        LocalTime end = toLocalTime(times[1]);
        LocalTime now = LocalTime.now();
        if (start.isBefore(end)) {
            return now.isAfter(start) && now.isBefore(end);
        }
        // 跨越午夜的情况
        return now.isAfter(start) || now.isBefore(end);
    }

    public boolean needWarn(double actualValue) {
        return OperatorType.getByType(operatorType).isTrue(actualValue, threshold);
    }

    private LocalTime toLocalTime(String time) {
        String[] times = time.split(":");
        return LocalTime.of(Integer.parseInt(times[0]), Integer.parseInt(times[1]));
    }

    public String getOperandDesc() {
        return getOperandTypeObj().desc;
    }

    public OperandType getOperandTypeObj() {
        return OperandType.getByType(operandType);
    }

    public String getOperatorDesc() {
        return OperatorType.getByType(operatorType).desc;
    }

    public boolean isTraffic1HourOperand() {
        return operandType == OperandType.TRAFFIC_1_HOUR.type;
    }

    public boolean isTraffic1DayOperand() {
        return operandType == OperandType.TRAFFIC_1_DAY.type;
    }

    public boolean isTrafficHourToHourOperand() {
        return operandType == OperandType.TRAFFIC_HOUR_TO_HOUR.type;
    }

    public boolean isTrafficDayToDayOperand() {
        return operandType == OperandType.TRAFFIC_DAY_TO_DAY.type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getThresholdString() {
        return appendSuffix(numberFormat(threshold));
    }

    private static String numberFormat(double value) {
        return new DecimalFormat("#.###").format(value);
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getWarnInterval() {
        return warnInterval;
    }

    public String getWarnIntervalString() {
        if (warnInterval == 0) {
            return "无限制";
        }
        return warnInterval + "分钟";
    }

    public int getWarnIntervalInMillis() {
        return warnInterval * 60 * 1000;
    }

    public void setWarnInterval(int warnInterval) {
        this.warnInterval = warnInterval;
    }

    public String getWarnTime() {
        return warnTime;
    }

    public void setWarnTime(String warnTime) {
        this.warnTime = warnTime;
    }

    public double getActualValue() {
        return actualValue;
    }

    public String getActualValueString() {
        return appendSuffix(numberFormat(actualValue));
    }

    private String appendSuffix(String value) {
        return value + (getOperandTypeObj().isPercentType() ? "%" : "条");
    }

    public TopicWarnConfig chooseTopicWarnConfig(AlarmConfigBridingService alarmConfigBridingService, String topicName) {
        WarnConfigStrategy topicWarnConfig = null;
        OperandType type = OperandType.getByType(operandType);
        switch (type) {
            case TRAFFIC_5_MIN:
                topicWarnConfig = new MinuteWarnConfig(this);
                break;
            case TRAFFIC_1_HOUR:
                topicWarnConfig = new HourWarnConfig(this);
                break;
            case TRAFFIC_1_DAY:
                topicWarnConfig = new DayWarnConfig(this);
                break;
            case TRAFFIC_MINUTE5_TO_MINUTE5:
                topicWarnConfig = new Minute5ToMinute5WarnConfig(this);
                break;
            case TRAFFIC_HOUR_TO_HOUR:
                topicWarnConfig = new HourToHourWarnConfig(this);
                break;
            case TRAFFIC_DAY_TO_DAY:
                topicWarnConfig = new DayToDayWarnConfig(this);
                break;
        }
        topicWarnConfig.alarmConfigBridingService = alarmConfigBridingService;
        topicWarnConfig.topicName = topicName;
        return topicWarnConfig;
    }

    public void setActualValue(double actualValue) {
        this.actualValue = actualValue;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getEnabled() {
        return enabled;
    }

    public boolean enabled() {
        return enabled == 1;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "TopicWarnConfig{" +
                "id=" + id +
                ", tid=" + tid +
                ", operandType=" + operandType +
                ", operatorType=" + operatorType +
                ", threshold=" + threshold +
                ", warnInterval=" + warnInterval +
                ", warnTime='" + warnTime + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
