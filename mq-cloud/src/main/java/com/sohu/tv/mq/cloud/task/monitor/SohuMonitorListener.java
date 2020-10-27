package com.sohu.tv.mq.cloud.task.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.Connection;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.body.ProcessQueueInfo;
import org.apache.rocketmq.common.protocol.heartbeat.ConsumeType;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.common.protocol.topic.OffsetMovedEvent;
import org.apache.rocketmq.tools.monitor.DeleteMsgsEvent;
import org.apache.rocketmq.tools.monitor.FailedMsgs;
import org.apache.rocketmq.tools.monitor.MonitorListener;
import org.apache.rocketmq.tools.monitor.UndoneMsgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.bo.ConsumerBlock;
import com.sohu.tv.mq.cloud.bo.ConsumerClientStat;
import com.sohu.tv.mq.cloud.bo.ConsumerStat;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TypedUndoneMsgs;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserConsumer;
import com.sohu.tv.mq.cloud.dao.ConsumerStatDao;
import com.sohu.tv.mq.cloud.service.AlarmConfigBridingService;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.ConsumerClientStatService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.service.UserConsumerService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.util.CommonUtil;
/**
 * 监控搜狐实现
 * @author yongfeigao
 *
 */
@Component
public class SohuMonitorListener implements MonitorListener {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
    @Autowired
	private ConsumerStatDao consumerStatDao;
    
    @Autowired
    private UserConsumerService userConsumerService;
    
    @Autowired
    private TopicService topicService;
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AlarmConfigBridingService alarmConfigBridingService;
    
    private long time;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private ConsumerClientStatService consumerClientStatService;
    
	@Override
	public void beginRound() {
		time = System.currentTimeMillis();
		log.info("monitor begin");
	}

	@Override
	public void reportUndoneMsgs(UndoneMsgs undoneMsgs) {
        String topic = undoneMsgs.getTopic();
        // 忽略topic
        if(mqCloudConfigHelper.isIgnoreTopic(topic)) {
            return;
        }
        try {
            //保存堆积消息的consumer的状态
            consumerStatDao.saveConsumerStat(undoneMsgs.getConsumerGroup(), topic, 
                    (int)undoneMsgs.getUndoneMsgsTotal(), 
                    (int)undoneMsgs.getUndoneMsgsSingleMQ(), 
                    undoneMsgs.getUndoneMsgsDelayTimeMills());
        } catch (Exception e) {
            log.error("save {}",undoneMsgs ,e);
        }
        veriftAccumulateAlarm(undoneMsgs);     
	}
	
	/**
	 * 校验是否发送报警邮件
	 * @param topic
	 * @param undoneMsgs
	 */
    private void veriftAccumulateAlarm(UndoneMsgs undoneMsgs) {
        long accumulateTime = alarmConfigBridingService.getAccumulateTime(undoneMsgs.getConsumerGroup());
        long accumulateCount = alarmConfigBridingService.getAccumulateCount(undoneMsgs.getConsumerGroup());
        if (accumulateTime < 0 && accumulateCount < 0) {
            return;
        }
        if (undoneMsgs instanceof TypedUndoneMsgs) {
            // 广播模式消费者堆积，无法检测堆积时间
            if (!((TypedUndoneMsgs) undoneMsgs).isClustering()) {
                if (undoneMsgs.getUndoneMsgsTotal() > accumulateCount) {
                    accumulateWarn(undoneMsgs);
                }
            } else {
                // 发送报警
                if (undoneMsgs.getUndoneMsgsDelayTimeMills() > accumulateTime
                        && undoneMsgs.getUndoneMsgsTotal() > accumulateCount) {
                    accumulateWarn(undoneMsgs);
                }
            }
        }
    }
    
	/**
	 * 堆积报警
	 * @param undoneMsgs
	 */
	public void accumulateWarn(UndoneMsgs undoneMsgs) {
	    // 验证报警频率
        if (!alarmConfigBridingService.needWarn("accumulate", undoneMsgs.getTopic(), undoneMsgs.getConsumerGroup())) {
            return;
        }
	    TopicExt topicExt = getUserEmail(undoneMsgs.getTopic(), undoneMsgs.getConsumerGroup());
	    if(topicExt == null) {
	        return;
	    }
        String content = getAccumulateWarnContent(topicExt.getTopic(), undoneMsgs);
        alertService.sendWarnMail(topicExt.getReceiver(), "堆积", content);
	}
	
	/**
	 * 获取用户邮件地址
	 * @param topic
	 * @param userID
	 * @return
	 */
	private TopicExt getUserEmail(String topic, String consumerGroup) {
	    // 获取topic
        Result<Topic> topicResult = topicService.queryTopic(topic);
        if(topicResult.isNotOK()) {
            return null;
        }
        TopicExt topicExt = new TopicExt();
        topicExt.setTopic(topicResult.getResult());
        // 获取用户
        Set<Long> userID = getUserID(topicResult.getResult().getId(), consumerGroup);
        String receiver = null;
        // 获取用户id
        if(!userID.isEmpty()) {
            // 获取用户信息
            Result<List<User>> userListResult = userService.query(userID);
            StringBuilder sb = new StringBuilder();
            if(userListResult.isNotEmpty()) {
                for(User u : userListResult.getResult()) {
                    sb.append(u.getEmail());
                    sb.append(",");
                }
            }
            if(sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
                receiver = sb.toString();
            }
        }
        topicExt.setReceiver(receiver);
        return topicExt;
	}
	
	/**
     * 获取用户ID
     * @param topic
     * @param consuemrGroup
     * @return
     */
    private Set<Long> getUserID(long tid, String consuemrGroup) {
        // 获取用户id
        Set<Long> uidList = new HashSet<Long>();
        Result<List<UserConsumer>> udListResult = userConsumerService.queryByNameAndTid(tid, consuemrGroup);
        if(udListResult.isNotEmpty()) {   
            for(UserConsumer uc : udListResult.getResult()) {
                uidList.add(uc.getUid());
            }    
        }
        return uidList;
    }
    
	/**
	 * 获取堆积预警信息
	 * @param topic
	 * @param undoneMsgs
	 * @return
	 */
    private String getAccumulateWarnContent(Topic topic, UndoneMsgs undoneMsgs) {
        StringBuilder content = new StringBuilder("详细如下:<br><br>");
        content.append("topic：<b>");
        content.append(topic.getName());
        content.append("</b> 的消费者：<b>");
        content.append(mqCloudConfigHelper.getTopicConsumeLink(topic.getId(), undoneMsgs.getConsumerGroup()));
        content.append("</b> 检测到堆积，总堆积消息量：");
        content.append(undoneMsgs.getUndoneMsgsTotal());
        content.append("，单个队列最大堆积消息量：");
        content.append(undoneMsgs.getUndoneMsgsSingleMQ());
        if (undoneMsgs.getUndoneMsgsDelayTimeMills() > 0) {
            content.append("，消费滞后时间(相对于broker最新消息时间)：");
            content.append(undoneMsgs.getUndoneMsgsDelayTimeMills() / 1000f);
            content.append("秒");
        }
        return content.toString();
    }

	@Override
	public void reportFailedMsgs(FailedMsgs failedMsgs) {
	}

	@Override
	public void reportDeleteMsgsEvent(DeleteMsgsEvent deleteMsgsEvent) {
		try {
			log.warn("receive offset event:{}", deleteMsgsEvent);
			OffsetMovedEvent event = deleteMsgsEvent.getOffsetMovedEvent(); 
			String consumerGroup = event.getConsumerGroup();
			if(MixAll.TOOLS_CONSUMER_GROUP.equals(consumerGroup)) {
			    return;
			}
			// 保存consume状态
			ConsumerStat consumerStat = new ConsumerStat();
            consumerStat.setConsumerGroup(consumerGroup);
            consumerStat.setTopic(event.getMessageQueue().getTopic());
            consumerStatDao.saveSimpleConsumerStat(consumerStat);
            int id = consumerStat.getId();
            
            // 保存block状态
			long time = deleteMsgsEvent.getEventTimestamp();
			String broker = event.getMessageQueue().getBrokerName();
			int qid = event.getMessageQueue().getQueueId();
			consumerStatDao.saveSomeConsumerBlock(id, broker, qid, time);
			
			// 预警
            offsetMoveWarn(deleteMsgsEvent);
		} catch (Exception e) {
			log.error("receive offset event:{}", deleteMsgsEvent, e);
		}
	}
	
	/**
     * 偏移量预警
     */
    public void offsetMoveWarn(DeleteMsgsEvent deleteMsgsEvent) {
        OffsetMovedEvent event = deleteMsgsEvent.getOffsetMovedEvent();
        TopicExt topicExt = getUserEmail(event.getMessageQueue().getTopic(), event.getConsumerGroup());
        if(topicExt == null) {
            return;
        }
        // 验证报警频率
        if (!alarmConfigBridingService.needWarn("offsetMove", event.getMessageQueue().getTopic(),
                event.getConsumerGroup())) {
            return;
        }
        StringBuilder content = new StringBuilder("详细如下:<br><br>");
        content.append("消费者：<b>");
        content.append(mqCloudConfigHelper.getTopicConsumeLink(topicExt.getTopic().getId(), event.getConsumerGroup()));
        content.append("</b> 偏移量错误，broker时间：<b>");
        content.append(DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(
                new Date(deleteMsgsEvent.getEventTimestamp())));
        content.append("</b> ，请求偏移量：<b>");
        content.append(event.getOffsetRequest());
        content.append("</b>，broker偏移量：<b>");
        content.append(event.getOffsetNew());
        content.append("</b>。队列信息如下：<br>");
        content.append("broker:");
        content.append(event.getMessageQueue().getBrokerName());
        content.append(" topic:");
        content.append(event.getMessageQueue().getTopic());
        content.append(" 队列:");
        content.append(event.getMessageQueue().getQueueId());
        alertService.sendWarnMail(topicExt.getReceiver(), "偏移量错误", content.toString());
    }

	@Override
	public void reportConsumerRunningInfo(
			TreeMap<String, ConsumerRunningInfo> criTable) {
		if(criTable == null || criTable.size() == 0) {
			return;
		}
		String consumerGroup = criTable.firstEntry().getValue().getProperties().getProperty("consumerGroup");
		try {
			// 分析订阅关系
			boolean result = ConsumerRunningInfo.analyzeSubscription(criTable);
			if (!result) {
				log.warn("ConsumerGroup: {}, Subscription different", consumerGroup);
				//同一个ConsumerGroup订阅了不同的topic，进行记录
				Set<SubscriptionData> set = new HashSet<SubscriptionData>();
				for(ConsumerRunningInfo info : criTable.values()) {
					set.addAll(info.getSubscriptionSet());
				}
				StringBuilder sb = new StringBuilder();
				Set<String> uniqSet = new HashSet<String>();
				for(SubscriptionData s : set) {
					if(CommonUtil.isRetryTopic(s.getTopic())) {
						continue;
					}
					String tmp = s.getTopic()+":"+s.getSubString();
					if(uniqSet.add(tmp)) {
					    sb.append(tmp);
					    sb.append(";");
					}
				}
				String sbscription = sb.toString();
				ConsumerStat consumerStat = new ConsumerStat();
				consumerStat.setConsumerGroup(consumerGroup);
				consumerStat.setSbscription(sbscription);
				consumerStatDao.saveSimpleConsumerStat(consumerStat);
				subscriptionWarn(consumerGroup, sbscription);
			}
		} catch (NumberFormatException e) {
			log.warn("num parse err");
		} catch (Exception e) {
			log.error("save subscription:{}", criTable, e);
		}

		// 分析客户端卡主的情况
		Map<TopicConsumer, List<ConsumerBlock>> map = new HashMap<TopicConsumer, List<ConsumerBlock>>();
		for(String clientId : criTable.keySet()) {
		    ConsumerRunningInfo info = criTable.get(clientId);
            String property = info.getProperties().getProperty(ConsumerRunningInfo.PROP_CONSUME_TYPE);
            if (property == null) {
                property = ((ConsumeType) info.getProperties().get(ConsumerRunningInfo.PROP_CONSUME_TYPE)).name();
            }
            // 只能分析push的情况
            if(ConsumeType.valueOf(property) != ConsumeType.CONSUME_PASSIVELY) {
                return;
            }

            String orderProperty = info.getProperties().getProperty(ConsumerRunningInfo.PROP_CONSUME_ORDERLY);
            boolean orderMsg = Boolean.parseBoolean(orderProperty);
            // 只分析非一致性消费
            if(orderMsg) {
                return;
            }

            Iterator<Entry<MessageQueue, ProcessQueueInfo>> it = info.getMqTable().entrySet().iterator();
            while (it.hasNext()) {
                Entry<MessageQueue, ProcessQueueInfo> next = it.next();
                MessageQueue mq = next.getKey();
                ProcessQueueInfo pq = next.getValue();
                long diff = System.currentTimeMillis() - pq.getLastConsumeTimestamp();
                if (diff < (1000 * 60) || pq.getCachedMsgCount() < 100) {
                    continue;
                }
                // 组装信息
                TopicConsumer tc = new TopicConsumer();
                tc.setTopic(mq.getTopic());
                tc.setConsumer(consumerGroup);
                List<ConsumerBlock> consumerBlockList = map.get(tc);
                if(consumerBlockList == null) {
                    consumerBlockList = new ArrayList<ConsumerBlock>();
                    map.put(tc, consumerBlockList);
                }
                ConsumerBlock cb = new ConsumerBlock();
                cb.setBlockTime(diff);
                cb.setInstance(clientId);
                cb.setBroker(mq.getBrokerName());
                cb.setQid(mq.getQueueId());
                consumerBlockList.add(cb);
            }
        }
		
		if(map.size() <= 0) {
		    return;
		}
		for(TopicConsumer tc : map.keySet()) {
		    ConsumerStat consumerStat = new ConsumerStat();
		    consumerStat.setConsumerGroup(tc.getConsumer());
		    consumerStat.setTopic(tc.getTopic());
		    consumerStatDao.saveSimpleConsumerStat(consumerStat);
		    int id = consumerStat.getId();
		    List<ConsumerBlock> list = map.get(tc);
		    for(ConsumerBlock cb : list) {
		        consumerStatDao.saveConsumerBlock(id, cb.getInstance(), cb.getBroker(), cb.getQid(), cb.getBlockTime());
		    }
		}
        // 报警
        blockWarn(map);
	}
	
	/**
     * 订阅报警
     */
    public void subscriptionWarn(String consumerGroup, String topics) {
        // 验证报警频率
        if (!alarmConfigBridingService.needWarn("subscribe", topics, consumerGroup)) {
            return;
        }
        StringBuilder content = new StringBuilder("详细如下:<br><br>");
        content.append("消费者：<b>");
        content.append(consumerGroup);
        content.append("</b> 同时订阅了：<b>");
        content.append(topics);
        content.append("</b>。");
        alertService.sendWarnMail(null, "订阅错误", content.toString());
    }
    
    /**
     * 客户端阻塞预警
     */
    public void blockWarn(Map<TopicConsumer, List<ConsumerBlock>> map) {
        for (TopicConsumer tc : map.keySet()) {
            Result<Topic> topicResult = topicService.queryTopic(tc.getTopic());
            if (topicResult.isNotOK()) {
                log.error("get topic err. topic:{}", tc.getTopic());
                continue;
            }
            List<ConsumerBlock> list = map.get(tc);
            // 获取预警配置
            long blockTime = alarmConfigBridingService.getBlockTime(tc.getConsumer());
            if (blockTime < 0) {
                continue;
            }
            // 验证报警频率
            if (!alarmConfigBridingService.needWarn("clientBlock", tc.getTopic(), tc.getConsumer())) {
                continue;
            }
            // 是否报警
            Iterator<ConsumerBlock> iterator = list.iterator();
            while(iterator.hasNext()) {
                ConsumerBlock consumerBlock = iterator.next();
                if(consumerBlock.getBlockTime() < blockTime) {
                    iterator.remove();
                }
            }
            if(list.size() <= 0) {
                continue;
            }
            
            StringBuilder content = new StringBuilder("详细如下:<br><br>");
            content.append("topic: <b>");
            content.append(tc.getTopic());
            content.append("</b> 的消费者：");
            content.append(mqCloudConfigHelper.getTopicConsumeLink(topicResult.getResult().getId(), tc.getConsumer()));
            content.append(" 检测到阻塞: <br>");
            content.append("<table border=1>");
            content.append("<thead>");
            content.append("<tr>");
            content.append("<th>clientId</th>");
            content.append("<th>broker</th>");
            content.append("<th>队列</th>");
            content.append("<th>阻塞时间</th>");
            content.append("</tr>");
            content.append("</thead>");
            content.append("<tbody>");
            for (ConsumerBlock cb : list) {
                content.append("<tr>");
                content.append("<td>");
                content.append(cb.getInstance());
                content.append("</td>");
                content.append("<td>");
                content.append(cb.getBroker());
                content.append("</td>");
                content.append("<td>");
                content.append(cb.getQid());
                content.append("</td>");
                content.append("<td>");
                content.append(cb.getBlockTime() / 1000f);
                content.append("秒</td>");
                content.append("</tr>");
            }
            content.append("</tbody>");
            content.append("</table>");
            TopicExt topicExt = getUserEmail(tc.getTopic(), tc.getConsumer());
            alertService.sendWarnMail(topicExt.getReceiver(), "客户端阻塞", content.toString());
        }
    }

    /**
     * 保存consumer-client信息
     * @param consumerGroup
     * @param cc
     */
    public void saveConsumerGroupClientInfo(String consumerGroup, ConsumerConnection cc) {
        for (Connection c : cc.getConnectionSet()) {
            String clientId = c.getClientId();
            // 解析出ip
            if (clientId.contains("@")) {
                clientId = clientId.split("@")[0];
                if (clientId.contains("-")) {
                    String[] s = clientId.split("-");
                    if (s.length > 1) {
                        clientId = clientId.substring(s[0].length() + 1);
                    }
                }
            } else {
                log.warn("consumer clientId is not recognized, clientId:{}", clientId);
            }
            ConsumerClientStat consumerClientStat = new ConsumerClientStat(consumerGroup, clientId);
            consumerClientStatService.save(consumerClientStat);
        }
    }

	@Override
	public void endRound() {
		long use = System.currentTimeMillis() - time;
		log.info("monitor end use:{}ms", use);
	}
	
	private class TopicExt {
	    private Topic topic;
	    private String receiver;
        public Topic getTopic() {
            return topic;
        }
        public void setTopic(Topic topic) {
            this.topic = topic;
        }
        public String getReceiver() {
            return receiver;
        }
        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }
	}
	
	private class TopicConsumer {
        private String topic;
        private String consumer;
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
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((consumer == null) ? 0 : consumer.hashCode());
            result = prime * result + ((topic == null) ? 0 : topic.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TopicConsumer other = (TopicConsumer) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (consumer == null) {
                if (other.consumer != null)
                    return false;
            } else if (!consumer.equals(other.consumer))
                return false;
            if (topic == null) {
                if (other.topic != null)
                    return false;
            } else if (!topic.equals(other.topic))
                return false;
            return true;
        }
        private SohuMonitorListener getOuterType() {
            return SohuMonitorListener.this;
        }
    }
}
