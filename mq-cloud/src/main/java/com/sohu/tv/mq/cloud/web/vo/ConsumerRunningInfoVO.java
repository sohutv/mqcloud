package com.sohu.tv.mq.cloud.web.vo;

import org.apache.rocketmq.remoting.protocol.body.ConsumeStatus;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.body.PopProcessQueueInfo;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.springframework.beans.BeanUtils;

import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 消费者客户端运行信息
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/8
 */
public class ConsumerRunningInfoVO {
    private Properties properties;

    private TreeSet<SubscriptionData> subscriptionSet;

    private TreeMap<String, ProcessQueueInfoVO> mqTable;

    private TreeMap<String, PopProcessQueueInfo> mqPopTable;

    private TreeMap<String/* Topic */, ConsumeStatus> statusTable;

    private TreeMap<String, String> userConsumerInfo;

    private String jstack;

    public static ConsumerRunningInfoVO toConsumerRunningInfoVO(ConsumerRunningInfo consumerRunningInfo) {
        ConsumerRunningInfoVO consumerRunningInfoVO = new ConsumerRunningInfoVO();
        if (consumerRunningInfo.getProperties().size() > 0) {
            consumerRunningInfoVO.properties = consumerRunningInfo.getProperties();
        }
        if (consumerRunningInfo.getSubscriptionSet().size() > 0) {
            consumerRunningInfoVO.subscriptionSet = consumerRunningInfo.getSubscriptionSet();
        }
        if (consumerRunningInfo.getMqPopTable().size() > 0) {
            consumerRunningInfoVO.mqPopTable = new TreeMap<>();
            consumerRunningInfo.getMqPopTable().forEach((k, v) -> {
                String key = k.getTopic() + ":" + k.getBrokerName() + ":" + k.getQueueId();
                consumerRunningInfoVO.mqPopTable.put(key, v);
            });
        }
        if (consumerRunningInfo.getStatusTable().size() > 0) {
            consumerRunningInfoVO.statusTable = consumerRunningInfo.getStatusTable();
        }
        if (consumerRunningInfo.getUserConsumerInfo().size() > 0) {
            consumerRunningInfoVO.userConsumerInfo = consumerRunningInfo.getUserConsumerInfo();
        }
        if (consumerRunningInfo.getJstack() != null) {
            consumerRunningInfoVO.jstack = consumerRunningInfo.getJstack();
        }
        if (consumerRunningInfo.getMqTable().size() > 0) {
            consumerRunningInfoVO.mqTable = new TreeMap<>();
            consumerRunningInfo.getMqTable().forEach((k, v) -> {
                String key = k.getTopic() + ":" + k.getBrokerName() + ":" + k.getQueueId();
                ProcessQueueInfoVO processQueueInfoVO = new ProcessQueueInfoVO();
                BeanUtils.copyProperties(v, processQueueInfoVO);
                consumerRunningInfoVO.mqTable.put(key, processQueueInfoVO);
            });
        }
        return consumerRunningInfoVO;
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

    public TreeMap<String, ProcessQueueInfoVO> getMqTable() {
        return mqTable;
    }

    public void setMqTable(TreeMap<String, ProcessQueueInfoVO> mqTable) {
        this.mqTable = mqTable;
    }

    public TreeMap<String, PopProcessQueueInfo> getMqPopTable() {
        return mqPopTable;
    }

    public void setMqPopTable(TreeMap<String, PopProcessQueueInfo> mqPopTable) {
        this.mqPopTable = mqPopTable;
    }

    public TreeMap<String, ConsumeStatus> getStatusTable() {
        return statusTable;
    }

    public void setStatusTable(TreeMap<String, ConsumeStatus> statusTable) {
        this.statusTable = statusTable;
    }

    public TreeMap<String, String> getUserConsumerInfo() {
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
}
