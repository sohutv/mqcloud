package com.sohu.tv.mq.rocketmq;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.mq.rocketmq.consumer.NORebalanceDefaultMQPullConsumer;
import com.sohu.tv.mq.util.CommonUtil;

/**
 * 支持消费某段时间内的消息
 * 
 * @author yongfeigao
 * @date 2021年11月18日
 */
@SuppressWarnings({"deprecation"})
public class TimespanConsumer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 消息拉取线程
    private Thread consumeThread;

    // 拉取模式消费者
    private DefaultMQPullConsumer pullConsumer;

    private RocketMQConsumer mqConsumer;

    // 消费的topic
    private String topic;

    // 消息开始时间
    private long start;

    // 消息结束时间
    private long end;

    public TimespanConsumer(RocketMQConsumer rocketMQConsumer, String topic, long start, long end) {
        this.topic = topic;
        this.mqConsumer = rocketMQConsumer;
        this.start = start;
        this.end = end;
        // 获取rpcHook
        RPCHook rpcHook = null;
        try {
            Field rpcHookField = DefaultMQPushConsumerImpl.class.getDeclaredField("rpcHook");
            rpcHookField.setAccessible(true);
            rpcHook = (RPCHook) rpcHookField.get(rocketMQConsumer.getConsumer().getDefaultMQPushConsumerImpl());
        } catch (Exception e) {
            logger.warn("get rpcHook error:{}", e.toString());
        }
        // 构建pullConsumer
        pullConsumer = new DefaultMQPullConsumer(MixAll.CID_RMQ_SYS_PREFIX + rocketMQConsumer.getGroup(), rpcHook);
        String nAddr = rocketMQConsumer.getConsumer().getDefaultMQPushConsumerImpl().getmQClientFactory()
                .getMQClientAPIImpl().getNameServerAddressList().stream().collect(Collectors.joining(";"));
        pullConsumer.setNamesrvAddr(nAddr);
        // 开始时间和结束时间作为实例id
        String instance = start + "@" + end;
        pullConsumer.setInstanceName(instance);
        try {
            // 为DefaultMQPullConsumer赋予NORebalanceDefaultMQPullConsumer
            Field field = DefaultMQPullConsumer.class.getDeclaredField("defaultMQPullConsumerImpl");
            field.setAccessible(true);
            field.set(pullConsumer, new NORebalanceDefaultMQPullConsumer(pullConsumer, rpcHook));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        consumeThread = new Thread(() -> consume(), topic + "@" + instance);
        consumeThread.setDaemon(true);
    }

    /**
     * 启动
     */
    public void start() {
        consumeThread.start();
    }

    /**
     * 消费
     */
    public void consume() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String startDate = sdf.format(new Date(start));
        String endDate = sdf.format(new Date(end));
        long time = System.currentTimeMillis();
        logger.info("topic:{} time[{},{}] consume begin", topic, startDate, endDate);
        try {
            // 启动
            pullConsumer.start();
            // 从slave拉取
            pullConsumer.getDefaultMQPullConsumerImpl().getPullAPIWrapper().setConnectBrokerByUser(true);
            pullConsumer.getDefaultMQPullConsumerImpl().getPullAPIWrapper().setDefaultBrokerId(MixAll.MASTER_ID + 1);
            // 获取消息队列
            Set<MessageQueue> mqs = pullConsumer.fetchSubscribeMessageQueues(topic);
            if (mqs == null || mqs.size() == 0) {
                logger.warn("{}'s messageQueue is empty!", topic);
                return;
            }
            long totalSize = 0;
            boolean isDLQ = CommonUtil.isDeadTopic(topic);
            for (MessageQueue mq : mqs) {
                // 获取偏移量
                long endOffset = pullConsumer.searchOffset(mq, end);
                long startOffset = pullConsumer.searchOffset(mq, start);
                // 处理非法情况
                if (startOffset >= endOffset) {
                    if (startOffset == 0) {
                        endOffset = 1;
                    } else {
                        endOffset = startOffset + 1;
                    }
                }
                logger.info("{}'s offset, start:{}, end:{}", mq, startOffset, endOffset);
                // 拉取消息
                while (startOffset < endOffset) {
                    PullResult pullResult = pullConsumer.pull(mq, "*", startOffset, 32);
                    // 防止offset不前进
                    if (startOffset < pullResult.getNextBeginOffset()) {
                        startOffset = pullResult.getNextBeginOffset();
                    } else {
                        ++startOffset;
                    }
                    // 无消息继续
                    if (PullStatus.FOUND != pullResult.getPullStatus()) {
                        continue;
                    }
                    // 时间过滤
                    List<MessageExt> msgs = new ArrayList<>();
                    for (MessageExt msg : pullResult.getMsgFoundList()) {
                        long msgTime = msg.getBornTimestamp();
                        if (isDLQ) {
                            msgTime = msg.getStoreTimestamp();
                        }
                        if (msgTime >= start && msgTime <= end) {
                            msgs.add(msg);
                            logger.info("fetch msgId:{} from:{}:{} time:{}", msg.getMsgId(),
                                    mq.getBrokerName(), mq.getQueueId(), sdf.format(new Date(msgTime)));
                        }
                    }
                    if (msgs.size() == 0) {
                        continue;
                    }
                    totalSize += msgs.size();
                    // 消费
                    if (mqConsumer.isConsumeOrderly()) {
                        mqConsumer.getMessageConsumer().consumeMessage(msgs, (ConsumeOrderlyContext) null);
                    } else {
                        mqConsumer.getMessageConsumer().consumeMessage(msgs, (ConsumeConcurrentlyContext) null);
                    }
                }
            }
            long use = System.currentTimeMillis() - time;
            logger.info("{} time[{},{}] size:{} use:{}ms", topic, startDate, endDate, totalSize, use);
        } catch (Exception e) {
            logger.error("{} consume start:{} end:{} error", topic, startDate, endDate, e);
        } finally {
            pullConsumer.shutdown();
        }
    }
}
