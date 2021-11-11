package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

/**
 * broker队列偏移量
 * 
 * @author yongfeigao
 * @date 2021年8月23日
 */
public class BrokersQueueOffsetVO {

    private String topic;

    private long topicId;

    private List<BrokerQueueOffset> brokerQueueOffsetList;

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<BrokerQueueOffset> getBrokerQueueOffsetList() {
        return brokerQueueOffsetList;
    }

    public void setBrokerQueueOffsetList(List<BrokerQueueOffset> brokerQueueOffsetList) {
        this.brokerQueueOffsetList = brokerQueueOffsetList;
    }

    public static class BrokerQueueOffset {
        // broker名
        private String broker;

        // 队列偏移量
        private List<QueueOffset> queueOffsetList;

        private long minOffset;
        private long maxOffset;
        private long messageCount;
        private long lastUpdateTimestamp = -1;

        public void calculate() {
            if (queueOffsetList == null || queueOffsetList.size() == 0) {
                return;
            }
            for (int i = 0; i < queueOffsetList.size(); ++i) {
                QueueOffset queueOffset = queueOffsetList.get(i);
                if (i == 0) {
                    minOffset = queueOffset.getMinOffset();
                }
                if (minOffset > queueOffset.getMinOffset()) {
                    minOffset = queueOffset.getMinOffset();
                }
                if (maxOffset < queueOffset.getMaxOffset()) {
                    maxOffset = queueOffset.getMaxOffset();
                }
                if (lastUpdateTimestamp < queueOffset.getLastUpdateTimestamp()) {
                    lastUpdateTimestamp = queueOffset.getLastUpdateTimestamp();
                }
                long count = queueOffset.getMaxOffset() - queueOffset.getMinOffset();
                if (count > 0) {
                    messageCount += count;
                }
            }
        }

        public long getMinOffset() {
            return minOffset;
        }

        public void setMinOffset(long minOffset) {
            this.minOffset = minOffset;
        }

        public long getMaxOffset() {
            return maxOffset;
        }

        public void setMaxOffset(long maxOffset) {
            this.maxOffset = maxOffset;
        }

        public long getMessageCount() {
            return messageCount;
        }

        public void setMessageCount(long messageCount) {
            this.messageCount = messageCount;
        }

        public long getLastUpdateTimestamp() {
            return lastUpdateTimestamp;
        }

        public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
            this.lastUpdateTimestamp = lastUpdateTimestamp;
        }

        public String getBroker() {
            return broker;
        }

        public void setBroker(String broker) {
            this.broker = broker;
        }

        public List<QueueOffset> getQueueOffsetList() {
            return queueOffsetList;
        }

        public void setQueueOffsetList(List<QueueOffset> queueOffsetList) {
            this.queueOffsetList = queueOffsetList;
        }
    }

    /**
     * 队列偏移量
     * 
     * @author yongfeigao
     * @date 2021年8月23日
     */
    public static class QueueOffset {
        private int queueId;
        private long minOffset;
        private long maxOffset;
        private long lastUpdateTimestamp;

        public int getQueueId() {
            return queueId;
        }

        public void setQueueId(int queueId) {
            this.queueId = queueId;
        }

        public long getMinOffset() {
            return minOffset;
        }

        public void setMinOffset(long minOffset) {
            this.minOffset = minOffset;
        }

        public long getMaxOffset() {
            return maxOffset;
        }

        public void setMaxOffset(long maxOffset) {
            this.maxOffset = maxOffset;
        }

        public long getLastUpdateTimestamp() {
            return lastUpdateTimestamp;
        }

        public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
            this.lastUpdateTimestamp = lastUpdateTimestamp;
        }
    }
}
