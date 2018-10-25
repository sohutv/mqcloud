package com.sohu.tv.mq.cloud.bo;

/**
 * 预警频率控制
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月8日
 */
public class NeedAlarmConfig {
    // 报警频率的key（type_topic_group）
    private String oKey;
    // 次数
    private long times;
    // 更新时间
    private long updateTime;
    
    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getoKey() {
        return oKey;
    }

    public void setoKey(String oKey) {
        this.oKey = oKey;
    }
}
