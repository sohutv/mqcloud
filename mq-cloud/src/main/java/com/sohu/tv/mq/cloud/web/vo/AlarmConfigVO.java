package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.bo.UserAlarmConfig;

/**
 * 报警配置
 * 
 * @author zhehongyuan
 * @date 2018年09月26日
 */
public class AlarmConfigVO {
    // 默认的报警配置
    private AlarmConfig defaultConfig;
    // 用户自定义的报警配置
    private List<UserAlarmConfig> userAlarmConfig;

    public AlarmConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(AlarmConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public List<UserAlarmConfig> getUserAlarmConfig() {
        return userAlarmConfig;
    }

    public void setUserAlarmConfig(List<UserAlarmConfig> userAlarmConfig) {
        this.userAlarmConfig = userAlarmConfig;
    }
}
