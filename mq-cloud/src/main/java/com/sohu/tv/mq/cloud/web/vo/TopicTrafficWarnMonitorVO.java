package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.TopicTrafficWarnConfig;

import java.util.List;

/**
 * @author yongweizhao
 * @create 2020/9/25 14:55
 */
public class TopicTrafficWarnMonitorVO {
    // 默认配置
    private TopicTrafficWarnConfig defaultConfig;
    // 自定义配置
    private List<TopicTrafficWarnConfig> customConfigList;

    public TopicTrafficWarnConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(TopicTrafficWarnConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public List<TopicTrafficWarnConfig> getCustomConfigList() {
        return customConfigList;
    }

    public void setCustomConfigList(List<TopicTrafficWarnConfig> customConfigList) {
        this.customConfigList = customConfigList;
    }
}
