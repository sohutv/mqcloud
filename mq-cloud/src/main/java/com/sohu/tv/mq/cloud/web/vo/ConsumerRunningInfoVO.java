package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.util.CommonUtil;
import com.sohu.tv.mq.util.JSONUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.body.ConsumeStatus;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.body.PopProcessQueueInfo;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.Map.Entry;

import static com.sohu.tv.mq.cloud.web.vo.ConsumerRunningInfoVO.QueueType.NORMAL_QUEUE;
import static com.sohu.tv.mq.cloud.web.vo.ConsumerRunningInfoVO.QueueType.RETRY_QUEUE;
import static com.sohu.tv.mq.cloud.web.vo.ConsumerRunningInfoVO.WarnType.*;

/**
 * 消费者客户端运行信息
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/8
 */
public class ConsumerRunningInfoVO {

    private Properties properties;

    // 订阅信息
    private TreeSet<SubscriptionData> subscriptionSet;

    // 重试队列
    private Map<String, ProcessQueueInfoVO> retryTopicProcessQueue;

    // 普通队列
    private Map<String, ProcessQueueInfoVO> topicProcessQueue;

    private Map<String, PopProcessQueueInfo> mqPopTable;

    private ConsumeStatus retryTopicConsumeStatus;

    private ConsumeStatus topicConsumeStatus;

    private Map<String, String> userConsumerInfo;

    // 是否预警
    private StringBuilder warnInfo;

    // 是否消费失败
    private boolean consumeFailed;

    // 是否流控
    private boolean flowControled;

    private String jstack;

    public ConsumerRunningInfoVO(ConsumerRunningInfo consumerRunningInfo) {
        this(consumerRunningInfo, null);
    }

    public ConsumerRunningInfoVO(ConsumerRunningInfo consumerRunningInfo, ConsumeStats consumeStats) {
        if (consumerRunningInfo.getProperties().size() > 0) {
            properties = consumerRunningInfo.getProperties();
        }
        if (consumerRunningInfo.getSubscriptionSet().size() > 0) {
            subscriptionSet = consumerRunningInfo.getSubscriptionSet();
        }
        if (consumerRunningInfo.getMqPopTable().size() > 0) {
            mqPopTable = new TreeMap<>();
            consumerRunningInfo.getMqPopTable().forEach((k, v) -> {
                String key = k.getTopic() + ":" + k.getBrokerName() + ":" + k.getQueueId();
                mqPopTable.put(key, v);
            });
        }
        if (consumerRunningInfo.getStatusTable().size() > 0) {
            for (Entry<String, ConsumeStatus> entry : consumerRunningInfo.getStatusTable().entrySet()) {
                if (CommonUtil.isRetryTopic(entry.getKey())) {
                    retryTopicConsumeStatus = entry.getValue();
                } else {
                    topicConsumeStatus = entry.getValue();
                }
            }
        }
        if (consumerRunningInfo.getUserConsumerInfo().size() > 0) {
            userConsumerInfo = consumerRunningInfo.getUserConsumerInfo();
        }
        if (consumerRunningInfo.getJstack() != null) {
            jstack = consumerRunningInfo.getJstack();
        }
        if (consumerRunningInfo.getMqTable().size() > 0) {
            consumerRunningInfo.getMqTable().forEach((k, v) -> {
                ProcessQueueInfoVO processQueueInfoVO = new ProcessQueueInfoVO(this);
                BeanUtils.copyProperties(v, processQueueInfoVO);
                if (consumeStats != null) {
                    OffsetWrapper offsetWrapper = consumeStats.getOffsetTable().get(k);
                    if (offsetWrapper != null) {
                        processQueueInfoVO.setBrokerOffset(offsetWrapper.getBrokerOffset());
                    }
                }
                getProcessQueueMap(k.getTopic()).put(k.getBrokerName() + ":" + k.getQueueId(), processQueueInfoVO);
            });
        }

        // 设置预警信息
        setWarnInfo();
    }

    private Map<String, ProcessQueueInfoVO> getProcessQueueMap(String topic) {
        if (CommonUtil.isRetryTopic(topic)) {
            if (retryTopicProcessQueue == null) {
                retryTopicProcessQueue = new TreeMap<>();
            }
            return retryTopicProcessQueue;
        } else {
            if (topicProcessQueue == null) {
                topicProcessQueue = new TreeMap<>();
            }
            return topicProcessQueue;
        }
    }

    /**
     * 设置预警信息
     */
    private void setWarnInfo() {
        setWarnInfo(RETRY_QUEUE, retryTopicProcessQueue);
        setWarnInfo(NORMAL_QUEUE, topicProcessQueue);
        if (topicConsumeStatus != null && topicConsumeStatus.getConsumeFailedMsgs() > 0) {
            addWarnInfo(NORMAL_QUEUE, CONSUME_FAILED, String.valueOf(topicConsumeStatus.getConsumeFailedMsgs()));
        }
        if (retryTopicConsumeStatus != null && retryTopicConsumeStatus.getConsumeFailedMsgs() > 0) {
            addWarnInfo(RETRY_QUEUE, CONSUME_FAILED, String.valueOf(retryTopicConsumeStatus.getConsumeFailedMsgs()));
        }
    }

    /**
     * 设置预警信息
     */
    private void setWarnInfo(QueueType queueType, Map<String, ProcessQueueInfoVO> processQueueMap) {
        if (processQueueMap == null) {
            return;
        }
        for (Entry<String, ProcessQueueInfoVO> entry : processQueueMap.entrySet()) {
            ProcessQueueInfoVO processQueueInfoVO = entry.getValue();
            // 队列被丢弃
            if (processQueueInfoVO.isDroped()) {
                addWarnInfo(queueType, DROP, entry.getKey());
                return;
            }
            // 拉取线程超时
            if (processQueueInfoVO.isLastPullLate()) {
                addWarnInfo(queueType, PULL_LATE, processQueueInfoVO.getLastPullTimestampFormat());
                return;
            }
            // 缓存消息量限流
            if (processQueueInfoVO.isCachedMsgCountOverThreshold()) {
                addWarnInfo(queueType, FLOW_CONTROL_COUNT,
                        processQueueInfoVO.getCachedMsgCount() + ">" + processQueueInfoVO.getPullThresholdForQueue());
                return;
            }
            // 缓存消息大小限流
            if (processQueueInfoVO.isCachedMsgSizeOverThreshold()) {
                addWarnInfo(queueType, FLOW_CONTROL_SIZE,
                        processQueueInfoVO.getCachedMsgSizeInMiB() + ">" + processQueueInfoVO.getPullThresholdSizeForQueue());
                return;
            }
            if (processQueueInfoVO.isMaxSpanOverThreshold()) {
                addWarnInfo(queueType, FLOW_CONTROL_SPAN,
                        processQueueInfoVO.getMaxSpan() + ">" + processQueueInfoVO.getConsumeConcurrentlyMaxSpan());
                return;
            }
        }
    }

    private void addWarnInfo(QueueType queueType, WarnType warnType, String message) {
        if (warnInfo == null) {
            warnInfo = new StringBuilder();
        }
        if (warnInfo.length() > 0) {
            warnInfo.append("<br/>");
        }
        if (queueType.getDesc() != null) {
            warnInfo.append(queueType.getDesc()).append(":");
        }
        warnInfo.append(warnType.getDesc()).append(":").append(message);
        if (CONSUME_FAILED == warnType) {
            consumeFailed = true;
        } else if (FLOW_CONTROL_COUNT == warnType || FLOW_CONTROL_SIZE == warnType || FLOW_CONTROL_SPAN == warnType) {
            flowControled = true;
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public TreeSet<SubscriptionData> getSubscriptionSet() {
        return subscriptionSet;
    }

    public void setSubscriptionSet(TreeSet<SubscriptionData> subscriptionSet) {
        this.subscriptionSet = subscriptionSet;
    }

    public Map<String, ProcessQueueInfoVO> getRetryTopicProcessQueue() {
        return retryTopicProcessQueue;
    }

    public void setRetryTopicProcessQueue(Map<String, ProcessQueueInfoVO> retryTopicProcessQueue) {
        this.retryTopicProcessQueue = retryTopicProcessQueue;
    }

    public Map<String, ProcessQueueInfoVO> getTopicProcessQueue() {
        return topicProcessQueue;
    }

    public void setTopicProcessQueue(Map<String, ProcessQueueInfoVO> topicProcessQueue) {
        this.topicProcessQueue = topicProcessQueue;
    }

    public Map<String, PopProcessQueueInfo> getMqPopTable() {
        return mqPopTable;
    }

    public void setMqPopTable(TreeMap<String, PopProcessQueueInfo> mqPopTable) {
        this.mqPopTable = mqPopTable;
    }

    public ConsumeStatus getRetryTopicConsumeStatus() {
        return retryTopicConsumeStatus;
    }

    public ConsumeStatus getTopicConsumeStatus() {
        return topicConsumeStatus;
    }

    public Map<String, String> getUserConsumerInfo() {
        return userConsumerInfo;
    }

    public void setUserConsumerInfo(TreeMap<String, String> userConsumerInfo) {
        this.userConsumerInfo = userConsumerInfo;
    }

    public String getJstack() {
        return jstack;
    }

    public void setJstack(String jstack) {
        this.jstack = jstack;
    }

    public String getWarnInfo() {
        return warnInfo == null ? null : warnInfo.toString();
    }

    public String getStartTimeFormat() {
        String startTimeStr = properties.getProperty("PROP_CONSUMER_START_TIMESTAMP");
        long startTime = NumberUtils.toLong(startTimeStr, 0);
        if (startTime <= 0) {
            return null;
        }
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(new Date(startTime));
    }

    public String getLanguage() {
        return properties.getProperty("language");
    }

    public boolean isConsumeOrderly() {
        return "true".equals(properties.getProperty("PROP_CONSUMEORDERLY"));
    }

    public int getPullThresholdForQueue() {
        return NumberUtils.toInt(properties.getProperty("pullThresholdForQueue"), 1000);
    }

    public int getPullThresholdSizeForQueue() {
        return NumberUtils.toInt(properties.getProperty("pullThresholdSizeForQueue"), 100);
    }

    public int getConsumeConcurrentlyMaxSpan() {
        return NumberUtils.toInt(properties.getProperty("consumeConcurrentlyMaxSpan"), 2000);
    }

    public String getConsumeMessageBatchMaxSize() {
        return properties.getProperty("consumeMessageBatchMaxSize");
    }

    public String getConsumeTimeout() {
        return properties.getProperty("consumeTimeout");
    }

    public String toJsonSting() {
        return JSONUtil.toJSONString(this);
    }

    public String getThreadpoolCoreSize() {
        String threadpoolCoreSize = properties.getProperty("PROP_THREADPOOL_CORE_SIZE");
        if (NumberUtils.toInt(threadpoolCoreSize, -1) > 0) {
            return threadpoolCoreSize;
        }
        return "未知";
    }

    public boolean isConsumeFailed() {
        return consumeFailed;
    }

    public boolean isFlowControled() {
        return flowControled;
    }

    enum QueueType {
        RETRY_QUEUE("重试消息"),
        NORMAL_QUEUE(null),
        ;
        private String desc;

        QueueType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    enum WarnType {
        DROP("已废弃"),
        PULL_LATE("拉取超时"),

        FLOW_CONTROL_COUNT("消费流控(数量)"),
        FLOW_CONTROL_SIZE("消费流控(大小)"),
        FLOW_CONTROL_SPAN("消费流控(跨度)"),

        CONSUME_FAILED("近一小时消费失败"),
        ;
        private String desc;

        WarnType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
