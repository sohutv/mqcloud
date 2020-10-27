package com.sohu.tv.mq.cloud.common.model;

import java.util.List;

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
/**
 * broker瞬时数据
 * 
 * @author yongfeigao
 * @date 2020年7月9日
 */
public class BrokerMomentStatsData extends RemotingSerializable {
    private List<BrokerMomentStatsItem> brokerMomentStatsItemList;
    // broker最大可用内存
    private long maxAccessMessageInMemory;

    public List<BrokerMomentStatsItem> getBrokerMomentStatsItemList() {
        return brokerMomentStatsItemList;
    }

    public void setBrokerMomentStatsItemList(List<BrokerMomentStatsItem> brokerMomentStatsItemList) {
        this.brokerMomentStatsItemList = brokerMomentStatsItemList;
    }

    public long getMaxAccessMessageInMemory() {
        return maxAccessMessageInMemory;
    }

    public void setMaxAccessMessageInMemory(long maxAccessMessageInMemory) {
        this.maxAccessMessageInMemory = maxAccessMessageInMemory;
    }
}
