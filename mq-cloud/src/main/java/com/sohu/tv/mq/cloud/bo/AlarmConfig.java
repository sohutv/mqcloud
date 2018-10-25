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

    private long id;
    // 用户id，为空行为默认配置
    private long uid;
    // topic名称，为空行为默认配置
    private String topic;
    // 堆积时间
    private long accumulateTime;
    // 堆积数量
    private long accumulateCount;
    // 堵塞时间
    private long blockTime;
    // 消费失败数量
    private long consumerFailCount;
    // 忽略堆积的主题
    private String ignoreTopic;
    // 单位时间，超过单位时间内的次数则不报警,单位小时
    private int warnUnitTime;
    // 单位时间内的次数
    private int warnUnitCount;
    // 报警总开关，是否接收报警
    private int ignoreWarn;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getAccumulateTime() {
        return accumulateTime;
    }

    public void setAccumulateTime(long accumulateTime) {
        this.accumulateTime = accumulateTime;
    }

    public long getAccumulateCount() {
        return accumulateCount;
    }

    public void setAccumulateCount(long accumulateCount) {
        this.accumulateCount = accumulateCount;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(long blockTime) {
        this.blockTime = blockTime;
    }

    public long getConsumerFailCount() {
        return consumerFailCount;
    }

    public void setConsumerFailCount(long consumerFailCount) {
        this.consumerFailCount = consumerFailCount;
    }

    public String getIgnoreTopic() {
        return ignoreTopic;
    }

    public void setIgnoreTopic(String ignoreTopic) {
        this.ignoreTopic = ignoreTopic;
    }

    public int getWarnUnitTime() {
        return warnUnitTime;
    }

    public void setWarnUnitTime(int warnUnitTime) {
        this.warnUnitTime = warnUnitTime;
    }

    public int getWarnUnitCount() {
        return warnUnitCount;
    }

    public void setWarnUnitCount(int warnUnitCount) {
        this.warnUnitCount = warnUnitCount;
    }

    public int getIgnoreWarn() {
        return ignoreWarn;
    }

    public void setIgnoreWarn(int ignoreWarn) {
        this.ignoreWarn = ignoreWarn;
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
        return warnUnitTime + "小时" + warnUnitCount + "次";
    }

}
