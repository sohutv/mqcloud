package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.util.WebUtil;

import java.util.List;

/**
 * broker内存落后
 * 
 * @author yongfeigao
 * @date 2021年9月22日
 */
public class BrokerFallBehind {
    private String broker;
    private String addr;
    // broker最大可用内存
    private long maxAccessMessageInMemory;

    private List<ConsumerFallBehind> list;

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public long getMaxAccessMessageInMemory() {
        return maxAccessMessageInMemory;
    }

    public String getMaxAccessMessageInMemoryFormat() {
        return WebUtil.sizeFormat(maxAccessMessageInMemory);
    }

    public void setMaxAccessMessageInMemory(long maxAccessMessageInMemory) {
        this.maxAccessMessageInMemory = maxAccessMessageInMemory;
    }

    public List<ConsumerFallBehind> getList() {
        return list;
    }

    public void setList(List<ConsumerFallBehind> list) {
        this.list = list;
    }

    public class ConsumerFallBehind {
        private String topic;
        private String consumer;
        private long accumulated;
        private String queue;
        private String consumerLink;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getConsumer() {
            return consumer;
        }

        public void setConsumer(String consumer) {
            this.consumer = consumer;
        }

        public long getAccumulated() {
            return accumulated;
        }

        public String getAccumulatedFormat() {
            return WebUtil.sizeFormat(accumulated);
        }

        public String getQueue() {
            return queue;
        }

        public void setQueue(String queue) {
            this.queue = queue;
        }

        public void setAccumulated(long accumulated) {
            this.accumulated = accumulated;
        }

        public String getConsumerLink() {
            return consumerLink;
        }

        public void setConsumerLink(String consumerLink) {
            this.consumerLink = consumerLink;
        }
    }
}
