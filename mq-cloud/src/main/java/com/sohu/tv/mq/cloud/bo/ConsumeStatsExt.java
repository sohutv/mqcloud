package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
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

    private boolean paused;

    private boolean disablePause;

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

    public String getClientIdHtml() {
        String[] sts = clientId.split(",");
        StringBuilder builder = new StringBuilder();
        for(String s : sts){
            builder.append("<div>");
            builder.append(s);
            builder.append("</div>");
        }
        return builder.toString();
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isDisablePause() {
        return disablePause;
    }

    public void setDisablePause(boolean disablePause) {
        this.disablePause = disablePause;
    }
}
