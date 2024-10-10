package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.ConsumeStatsExt;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.ConsumerConfig;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 消费进度
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月11日
 */
public class ConsumerProgressVO {
    private Consumer consumer;
    private double consumeTps;
    // 正常offset
    private Map<MessageQueue, OffsetWrapper> offsetMap;
    // 重试offset
    private Map<MessageQueue, OffsetWrapper> retryOffsetMap;
    // 死消息offset
    private Map<MessageQueue, TopicOffset> dlqOffsetMap;
    // 正常topic
    private String topic;
    // 重试topic
    private String retryTopic;
    // 死消息topic
    private String dlqTopic;

    private long lastTimestamp = Long.MAX_VALUE;
    private List<ConsumeStatsExt> consumeStatsList;
    private List<User> ownerList;
    // 总偏移量
    private long diffTotal;
    // 消费者配置
    private ConsumerConfig consumerConfig;

    // 客户端版本
    private String version;

    private long produceTps;

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public double getConsumeTps() {
        return consumeTps;
    }

    public String getConsumeTpsFormat() {
        return WebUtil.countFormat(Math.round(consumeTps));
    }

    public long getConsumeTpsRound() {
        return Math.round(consumeTps);
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
        // retry无数据不返回
        if (getRetryMaxOffset() == 0) {
            return null;
        }
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
        return diffTotal;
    }

    public String getDiffFormat() {
        return WebUtil.countFormat(diffTotal);
    }

    public long computeTotalDiff() {
        diffTotal = computeTotalDiff(offsetMap, false);
        diffTotal += computeTotalDiff(retryOffsetMap, true);
        if (diffTotal < 0) {
            diffTotal = 0;
        }
        return diffTotal;
    }

    private long computeTotalDiff(Map<MessageQueue, OffsetWrapper> map, boolean retry) {
        long diffTotal = 0L;
        if (map == null) {
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
            if (lastTimestamp == 0) {
                continue;
            }
            // 取非重试队列和重试队列有堆积的 最小时间
            if ((!retry || (retry && diff > 0)) && lastTimestamp < minReportTime) {
                minReportTime = lastTimestamp;
            }
        }

        if (lastTimestamp > minReportTime) {
            lastTimestamp = minReportTime;
        }
        return diffTotal;
    }

    public long getMaxOffset() {
        return getMaxOffset(offsetMap);
    }

    public long getRetryMaxOffset() {
        return getMaxOffset(retryOffsetMap);
    }

    private long getMaxOffset(Map<MessageQueue, OffsetWrapper> offsetMap) {
        long maxOffset = 0;
        if (offsetMap != null) {
            for (OffsetWrapper offsetWrapper : offsetMap.values()) {
                if (maxOffset < offsetWrapper.getBrokerOffset()) {
                    maxOffset = offsetWrapper.getBrokerOffset();
                }
            }
        }
        return maxOffset;
    }

    public long getDlqMaxOffset() {
        long maxOffset = 0;
        if (dlqOffsetMap != null) {
            for (TopicOffset topicOffset : dlqOffsetMap.values()) {
                if (maxOffset < topicOffset.getMaxOffset()) {
                    maxOffset = topicOffset.getMaxOffset();
                }
            }
        }
        return maxOffset;
    }

    public List<ConsumeStatsExt> getConsumeStatsList() {
        return consumeStatsList;
    }

    public void setConsumeStatsList(List<ConsumeStatsExt> consumeStatsList) {
        this.consumeStatsList = consumeStatsList;
    }

    public Map<MessageQueue, TopicOffset> getDlqOffsetMap() {
        return dlqOffsetMap;
    }

    public void setDlqOffsetMap(Map<MessageQueue, TopicOffset> dlqOffsetMap) {
        this.dlqOffsetMap = dlqOffsetMap;
    }

    public List<User> getOwnerList() {
        return ownerList;
    }

    public void setOwnerList(List<User> ownerList) {
        this.ownerList = ownerList;
    }

    public String getDlqTopic() {
        return dlqTopic;
    }

    public void setDlqTopic(String dlqTopic) {
        this.dlqTopic = dlqTopic;
    }

    public ConsumerConfig getConsumerConfig() {
        return consumerConfig;
    }

    public void setConsumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public void resetClientPauseConfig() {
        if (consumeStatsList == null || consumerConfig == null) {
            return;
        }
        for (ConsumeStatsExt consumeStatsExt : consumeStatsList) {
            if (CollectionUtils.isEmpty(consumerConfig.getPauseConfig())) {
                if (consumerConfig.getPause() != null && consumerConfig.getPause()) {
                    consumeStatsExt.setPaused(true);
                    consumeStatsExt.setDisablePause(true);
                }
            } else {
                if (consumerConfig.containsPauseConfig(consumeStatsExt.getClientId())) {
                    consumeStatsExt.setPaused(true);
                }
            }
        }
    }

    public String getMessageModel() {
        if (consumer.isClustering()) {
            return "集群";
        }
        return "广播";
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreateDateFormat() {
        return DateUtil.getFormat(DateUtil.YMD_DASH).format(consumer.getCreateDate());
    }

    public long getProduceTps() {
        return produceTps;
    }

    public void setProduceTps(long produceTps) {
        this.produceTps = produceTps;
    }

    public boolean needShowLeftTime() {
        if (consumer.isBroadcast()) {
            return false;
        }
        if (consumeTps == 0) {
            return false;
        }
        if (diffTotal < 10000) {
            return false;
        }
        return true;
    }

    /**
     * 剩余时间
     *
     * @return
     */
    public String getLeftTimeFormat() {
        double tps = consumeTps - produceTps;
        if (tps <= 0) {
            return "消费速度小于生产速度，无法计算剩余时间";
        }
        int leftTime = (int) (diffTotal / tps);
        int days = leftTime / 86400;
        int hours = (leftTime % 86400) / 3600;
        int minutes = (leftTime % 3600) / 60;
        StringBuilder sb = new StringBuilder("预计还需");
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分");
        }
        sb.append("消费完");
        return sb.toString();
    }
}
