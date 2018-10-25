package com.sohu.tv.mq.cloud.bo;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.message.MessageQueue;
/**
 * ConsumeStats扩展
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月31日
 */
public class ConsumeStatsExt {
    
    private Map<MessageQueue, OffsetWrapper> offsetTable = new TreeMap<MessageQueue, OffsetWrapper>();
    
    private double consumeTps = 0;
    
    private String clientId;

    public long computeTotalDiff() {
        long diffTotal = 0L;

        Iterator<Entry<MessageQueue, OffsetWrapper>> it = getOffsetTable().entrySet().iterator();
        while (it.hasNext()) {
            Entry<MessageQueue, OffsetWrapper> next = it.next();
            long diff = next.getValue().getBrokerOffset() - next.getValue().getConsumerOffset();
            diffTotal += diff;
        }

        return diffTotal;
    }

    public Map<MessageQueue, OffsetWrapper> getOffsetTable() {
        return offsetTable;
    }

    public void setOffsetTable(Map<MessageQueue, OffsetWrapper> offsetTable) {
        this.offsetTable = offsetTable;
    }

    public double getConsumeTps() {
        return consumeTps;
    }

    public void setConsumeTps(double consumeTps) {
        this.consumeTps = consumeTps;
    }
    
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
