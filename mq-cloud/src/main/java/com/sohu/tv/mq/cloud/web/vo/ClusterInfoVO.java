package com.sohu.tv.mq.cloud.web.vo;

import java.util.Map;

import com.sohu.tv.mq.cloud.bo.Cluster;

/**
 * 集群信息
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月9日
 */
public class ClusterInfoVO {
    // 是否具有names erver了
    private boolean hasNameServer;
    
    private Cluster[] mqCluster;
    
    private Cluster selectedMQCluster;

    // broker 信息
    private Map<String/* brokerName */, Map<String/* brokerId */, BrokerStatVO>> brokerGroup;

    public Cluster[] getMqCluster() {
        return mqCluster;
    }

    public Cluster getSelectedMQCluster() {
        return selectedMQCluster;
    }

    public void setSelectedMQCluster(Cluster selectedMQCluster) {
        this.selectedMQCluster = selectedMQCluster;
    }

    public void setMqCluster(Cluster[] mqCluster) {
        this.mqCluster = mqCluster;
    }

    public Map<String, Map<String, BrokerStatVO>> getBrokerGroup() {
        return brokerGroup;
    }

    public void setBrokerGroup(Map<String, Map<String, BrokerStatVO>> brokerGroup) {
        this.brokerGroup = brokerGroup;
    }

    public boolean isHasNameServer() {
        return hasNameServer;
    }

    public void setHasNameServer(boolean hasNameServer) {
        this.hasNameServer = hasNameServer;
    }
}
