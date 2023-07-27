package com.sohu.tv.mq.route;

import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.client.consumer.rebalance.AbstractAllocateMessageQueueStrategy;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 亲和性消息队列分配
 *
 * @author: yongfeigao
 * @date: 2022/11/17 14:38
 */
public class AllocateMessageQueueByAffinity extends AllocateMessageQueueAveragely {

    private static final Logger log = LoggerFactory.getLogger(AllocateMessageQueueByAffinity.class);

    @Override
    public List<MessageQueue> allocate(String consumerGroup, String currentCID, List<MessageQueue> mqAll,
                                       List<String> cidAll) {
        try {
            // cid亲和性分组
            Map<String, List<String>> cidAffinityMap = groupCidToAffinityMap(cidAll);
            if (cidAffinityMap == null) {
                log.info("{}:no affinity, cidAffinityMap is null", consumerGroup);
                return super.allocate(consumerGroup, currentCID, mqAll, cidAll);
            }
            // 消息队列亲和性分组
            Map<String, List<MessageQueue>> mqAffinityMap = groupMessageQueueToAffinityMap(mqAll);
            if (!cidAffinityMap.keySet().equals(mqAffinityMap.keySet())) {
                // 只有broker的机房的亲和标记和客户端的亲和标记完全匹配时，才执行亲和，原因如下：
                // 1.如果broker部署的机房，没有消费者亲和，会导致该机房broker的消息无法消费（尤其在新增机房时）。
                // 2.如果客户端亲和标记多于broker部署的机房（设置错误或机房下线），亲和分配会导致队列分配混乱。
                log.info("{}:no affinity, cidAffinity:{}, brokerAffinity:{}", consumerGroup, cidAffinityMap.keySet(),
                        mqAffinityMap.keySet());
                return super.allocate(consumerGroup, currentCID, mqAll, cidAll);
            }
            // 获取亲和性标记
            String affinityFlag = getAffinityFlag(currentCID);
            if (affinityFlag == null) {
                log.info("{}:no affinity, affinityFlag is null", consumerGroup);
                return super.allocate(consumerGroup, currentCID, mqAll, cidAll);
            }
            // 获取亲和性cid
            List<String> affinityCidList = cidAffinityMap.get(affinityFlag);
            if (affinityCidList == null) {
                log.info("{}:no affinity, affinityCidList is null", consumerGroup);
                return super.allocate(consumerGroup, currentCID, mqAll, cidAll);
            }

            // 获取亲和性队列
            List<MessageQueue> affinityMQList = mqAffinityMap.get(affinityFlag);
            if (affinityMQList == null) {
                log.info("{}:no affinity, affinityMQList is null", consumerGroup);
                return super.allocate(consumerGroup, currentCID, mqAll, cidAll);
            }
            log.info("{}:affinity, affinityCidList:{}, affinityMQList:{}", consumerGroup, affinityCidList,
                    affinityMQList);
            return super.allocate(consumerGroup, currentCID, affinityMQList, affinityCidList);
        } catch (Exception e) {
            log.error("{}:affinity cid:{} cidAll:{} mqAll:{} error", consumerGroup, currentCID, cidAll, mqAll, e);
            return super.allocate(consumerGroup, currentCID, mqAll, cidAll);
        }
    }

    /**
     * cid亲和性分组
     *
     * @param cidAll
     * @return
     */
    public Map<String, List<String>> groupCidToAffinityMap(List<String> cidAll) {
        Map<String, List<String>> affinityCidMap = new HashMap<>();
        for (String cid : cidAll) {
            int idx = cid.indexOf(CommonUtil.MQ_AFFINITY_DELIMITER);
            // 非最新客户端不分组
            if (idx == -1) {
                return null;
            }
            String[] cidArray = cid.substring(idx + 1).split("@", 2);
            affinityCidMap.computeIfAbsent(cidArray[0], k -> new ArrayList<>()).add(cid);
        }
        return affinityCidMap;
    }

    /**
     * 消息队列亲和性分组
     *
     * @param cidAll
     * @return
     */
    public Map<String, List<MessageQueue>> groupMessageQueueToAffinityMap(List<MessageQueue> mqAll) {
        Map<String, List<MessageQueue>> affinityMessageQueueMap = new HashMap<>();
        for (MessageQueue mq : mqAll) {
            String brokerName = mq.getBrokerName();
            String[] array = brokerName.split(CommonUtil.MQ_AFFINITY_DELIMITER, 2);
            String affinitySuffix = CommonUtil.MQ_AFFINITY_DEFAULT;
            if (array.length == 2) {
                affinitySuffix = array[1];
            }
            affinityMessageQueueMap.computeIfAbsent(affinitySuffix, k -> new ArrayList<>()).add(mq);
        }
        return affinityMessageQueueMap;
    }

    /**
     * 获取cid亲和标记
     *
     * @param cid
     * @return
     */
    public String getAffinityFlag(String cid) {
        int idx = cid.indexOf(CommonUtil.MQ_AFFINITY_DELIMITER);
        // 非最新客户端不执行亲和
        if (idx == -1) {
            return null;
        }
        return cid.substring(idx + 1).split("@", 2)[0];
    }

    @Override
    public String getName() {
        return "Affinity";
    }
}
