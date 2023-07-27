package com.sohu.tv.mq.cloud.bo;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.remoting.protocol.DataVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;

/**
 * 订阅组
 * 
 * @author yongfeigao
 * @date 2019年8月15日
 */
public class SubscriptionGroup {
    // 订阅信息
    private Map<String, SubscriptionGroupConfig> subscriptionGroupTable = new HashMap<String, SubscriptionGroupConfig>();

    // 数据版本
    private DataVersion dataVersion = new DataVersion();

    public Map<String, SubscriptionGroupConfig> getSubscriptionGroupTable() {
        return subscriptionGroupTable;
    }

    public void setSubscriptionGroupTable(Map<String, SubscriptionGroupConfig> subscriptionGroupTable) {
        this.subscriptionGroupTable = subscriptionGroupTable;
    }

    public DataVersion getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(DataVersion dataVersion) {
        this.dataVersion = dataVersion;
    }

    /**
     * 构建监控订阅组
     * @return
     */
    public static SubscriptionGroup buildMonitorSubscriptionGroup() {
        SubscriptionGroupConfig subscriptionGroupConfig = new SubscriptionGroupConfig();
        subscriptionGroupConfig.setGroupName(MixAll.MONITOR_CONSUMER_GROUP);
        SubscriptionGroup subscriptionGroup = new SubscriptionGroup();
        subscriptionGroup.getSubscriptionGroupTable().put(MixAll.MONITOR_CONSUMER_GROUP, subscriptionGroupConfig);
        return subscriptionGroup;
    }
}
