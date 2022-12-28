package com.sohu.tv.mq.route;

import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.client.impl.producer.TopicPublishInfo;
import org.apache.rocketmq.client.latency.MQFaultStrategy;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.List;

/**
 * 亲和性MQ策略
 *
 * @author: yongfeigao
 * @date: 2022/11/16 17:26
 */
public class AffinityMQStrategy extends MQFaultStrategy {

    // 亲和的broker后缀
    private String affinityBrokerSuffix;

    private boolean affinityIfBrokerNotSet;

    public AffinityMQStrategy() {
    }

    public AffinityMQStrategy(String affinityBrokerSuffix, boolean affinityIfBrokerNotSet) {
        this.affinityBrokerSuffix = affinityBrokerSuffix;
        this.affinityIfBrokerNotSet = affinityIfBrokerNotSet;
    }

    @Override
    public MessageQueue selectOneMessageQueue(TopicPublishInfo tpInfo, String lastBrokerName) {
        List<MessageQueue> messageQueueList = tpInfo.getMessageQueueList();
        for (int i = 0; i < messageQueueList.size(); i++) {
            int index = tpInfo.getSendWhichQueue().incrementAndGet();
            int pos = Math.abs(index) % messageQueueList.size();
            if (pos < 0) {
                pos = 0;
            }
            MessageQueue mq = messageQueueList.get(pos);
            if (mq.getBrokerName().equals(lastBrokerName)) {
                continue;
            }
            // broker没有亲和性后缀，客户端要求亲和，则选择该broker
            if (!mq.getBrokerName().contains(CommonUtil.MQ_AFFINITY_DELIMITER) && affinityIfBrokerNotSet) {
                return mq;
            }
            if (mq.getBrokerName().endsWith(affinityBrokerSuffix)) {
                return mq;
            }
        }
        // 没有任何broker时的保底策略
        return tpInfo.selectOneMessageQueue();
    }
}
