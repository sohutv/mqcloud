package com.sohu.tv.mq.cloud.web.vo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.message.MessageQueue;

import com.sohu.tv.mq.cloud.bo.ConsumeStatsExt;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.User;
/**
 * 消费进度
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月11日
 */
public class ConsumerProgressVO {
    private Consumer consumer;
    private double consumeTps;
    private Map<MessageQueue, OffsetWrapper> offsetMap;
    private Map<MessageQueue, OffsetWrapper> retryOffsetMap;
    private String retryTopic;
    private String topic;
    private long lastTimestamp = Long.MAX_VALUE;
    private List<ConsumeStatsExt> consumeStatsList;
    private List<User> ownerList;
    public Consumer getConsumer() {
        return consumer;
    }
    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }
    public double getConsumeTps() {
        return consumeTps;
    }
    public void setConsumeTps(double consumeTps) {
        this.consumeTps = consumeTps;
    }
    public Map<MessageQueue, OffsetWrapper> getOffsetMap() {
        return offsetMap;
    }
    public void setOffsetMap(Map<MessageQueue, OffsetWrapper> offsetMap) {
        this.offsetMap = offsetMap;
    }
    public Map<MessageQueue, OffsetWrapper> getRetryOffsetMap() {
        return retryOffsetMap;
    }
    public void setRetryOffsetMap(Map<MessageQueue, OffsetWrapper> retryOffsetMap) {
        this.retryOffsetMap = retryOffsetMap;
    }
    public long getMinLastTimestamp() {
        return lastTimestamp;
    }
    public long getLastTimestamp() {
        return lastTimestamp;
    }
    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }
    public String getRetryTopic() {
        return retryTopic;
    }
    public void setRetryTopic(String retryTopic) {
        this.retryTopic = retryTopic;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public long getDiff() {
        long diffTotal = computeTotalDiff(offsetMap, false);
        diffTotal += computeTotalDiff(retryOffsetMap, true);
        if(diffTotal < 0) {
            diffTotal = 0;
        }
        return diffTotal;
    }
    
    private long computeTotalDiff(Map<MessageQueue, OffsetWrapper> map, boolean retry) {
        long diffTotal = 0L;
        if(map == null) {
            return diffTotal;
        }
        // 最小时间
        long minReportTime = Long.MAX_VALUE;
        Iterator<Entry<MessageQueue, OffsetWrapper>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<MessageQueue, OffsetWrapper> next = it.next();
            long diff = next.getValue().getBrokerOffset() - next.getValue().getConsumerOffset();
            diffTotal += diff;
            
            long lastTimestamp = next.getValue().getLastTimestamp();
            if(lastTimestamp == 0) {
                continue;
            }
            // 取非重试队列和重试队列有堆积的  最小时间
            if((!retry || (retry && diff > 0)) && lastTimestamp < minReportTime) {
                minReportTime = lastTimestamp;
            }
        }
        
        if(lastTimestamp > minReportTime) {
            lastTimestamp = minReportTime;
        }
        return diffTotal;
    }
    public List<ConsumeStatsExt> getConsumeStatsList() {
        return consumeStatsList;
    }
    public void setConsumeStatsList(List<ConsumeStatsExt> consumeStatsList) {
        this.consumeStatsList = consumeStatsList;
    }
    public List<User> getOwnerList() {
        return ownerList;
    }
    public void setOwnerList(List<User> ownerList) {
        this.ownerList = ownerList;
    }
}
