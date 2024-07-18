package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;
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
    // 是否具有nameserver了
    private boolean hasNameServer;
    
    private Cluster[] mqCluster;
    
    private Cluster selectedMQCluster;

    // broker 信息
    private Map<String/* brokerName */, List<BrokerStatVO>> brokerGroup;

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

    public Map<String, List<BrokerStatVO>> getBrokerGroup() {
        return brokerGroup;
    }

    public void setBrokerGroup(Map<String, List<BrokerStatVO>> brokerGroup) {
        this.brokerGroup = brokerGroup;
    }

    public boolean isHasNameServer() {
        return hasNameServer;
    }

    public void setHasNameServer(boolean hasNameServer) {
        this.hasNameServer = hasNameServer;
    }
}
