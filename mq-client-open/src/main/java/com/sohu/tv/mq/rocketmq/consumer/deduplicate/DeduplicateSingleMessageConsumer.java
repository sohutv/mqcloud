package com.sohu.tv.mq.rocketmq.consumer.deduplicate;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.common.message.MessageClientExt;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;

import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.rocketmq.RocketMQConsumer;
import com.sohu.tv.mq.rocketmq.RocketMQProducer.MessageDelayLevel;
import com.sohu.tv.mq.rocketmq.consumer.SingleMessageConsumer;
import com.sohu.tv.mq.rocketmq.redis.degradable.RedisGetCommand;
import com.sohu.tv.mq.rocketmq.redis.degradable.RedisSetCommand;
import com.sohu.tv.mq.util.CommonUtil;

import redis.clients.jedis.params.SetParams;

/**
 * 单个消息去重消费
 * 
 * @author yongfeigao
 * @date 2021年9月1日
 * @param <T>
 */
public class DeduplicateSingleMessageConsumer<T> extends SingleMessageConsumer<T> {

    // 标识消息消费中
    private static final String CONSUMING = "0";
    // 标识消息消费失败
    private static final String CONSUME_FAILED = "1";
    // 标识消息消费成功
    private static final String CONSUME_OK = "2";
    // 标识延迟消息
    private static final String DELAY_MESSAGE = "delay";

    public DeduplicateSingleMessageConsumer(RocketMQConsumer rocketMQConsumer) {
        super(rocketMQConsumer);
    }

    @Override
    public void consume(T message, MessageExt msgExt) throws Exception {
        // 重试消息但非延迟消息直接消费
        if (CommonUtil.isRetryTopic(msgExt.getProperty(MessageConst.PROPERTY_REAL_TOPIC)) &&
                msgExt.getProperty(DELAY_MESSAGE) == null) {
            super.consume(message, msgExt);
            return;
        }
        // 构建去重key
        String key = buildKey(msgExt);
        if (key == null) {
            super.consume(message, msgExt);
            return;
        }
        // 获取offsetMsgId
        String offsetMsgId = getOffsetMsgId(msgExt);
        if (offsetMsgId == null) {
            super.consume(message, msgExt);
            return;
        }
        // 设置正在消费标识
        if (setConsumingFlag(key, offsetMsgId)) {
            try {
                super.consume(message, msgExt);
                setConsumeOKFlag(key, offsetMsgId);
            } catch (Exception e) {
                setConsumeFailedFlag(key, offsetMsgId);
                throw e;
            }
            return;
        }
        // 获取标识
        Pair<String, String> pair = getFlag(key);
        // 获取失败或真的不存在，直接消费
        if (pair == null) {
            super.consume(message, msgExt);
            return;
        }
        // offsetMsgId相同且正在消费
        if (offsetMsgId.equals(pair.getObject1()) && CONSUMING.equals(pair.getObject2())) {
            if (sendDelayMessage(rocketMQConsumer.getGroup(), msgExt)) {
                return;
            }
            // 发送失败直接消费
            super.consume(message, msgExt);
            return;
        }
        logger.info("msg:{} offsetMsgId:{} flag:{} duplicate offsetMsgId:{}, reconsumeTimes:{}", key, offsetMsgId,
                pair.getObject2(), pair.getObject1(), msgExt.getReconsumeTimes());
    }

    /**
     * 设置正在消费标识
     * 
     * @param key
     * @param offsetMsgId
     * @return
     */
    private boolean setConsumingFlag(String key, String offsetMsgId) {
        SetParams setParams = SetParams.setParams().nx().ex(rocketMQConsumer.getDeduplicateWindowSeconds());
        return setFlag(key, CONSUMING, offsetMsgId, setParams);
    }

    /**
     * 设置消费成功标识
     * 
     * @param key
     * @param offsetMsgId
     * @return
     */
    private boolean setConsumeOKFlag(String key, String offsetMsgId) {
        SetParams setParams = SetParams.setParams().xx().ex(rocketMQConsumer.getDeduplicateWindowSeconds());
        return setFlag(key, CONSUME_OK, offsetMsgId, setParams);
    }

    /**
     * 设置消费失败标识
     * 
     * @param key
     * @param offsetMsgId
     * @return
     */
    private boolean setConsumeFailedFlag(String key, String offsetMsgId) {
        SetParams setParams = SetParams.setParams().xx().ex(rocketMQConsumer.getDeduplicateWindowSeconds());
        return setFlag(key, CONSUME_FAILED, offsetMsgId, setParams);
    }

    /**
     * 设置标识
     * 
     * @param key
     * @param flag
     * @param msgExt
     * @return
     */
    private boolean setFlag(String key, String flag, String offsetMsgId, SetParams setParams) {
        try {
            String value = offsetMsgId + ":" + flag;
            Result<String> result = new RedisSetCommand(rocketMQConsumer.getRedis(), key, value, setParams).execute();
            if (result.isSuccess()) {
                return "OK".equals(result.getResult());
            }
        } catch (Exception e) {
            logger.warn("setFlag:{} key:{} error:{}", flag, key, e.toString());
        }
        // 异常状况当做设置成功
        return true;
    }

    /**
     * 获取标识
     * 
     * @param key
     * @return
     */
    private Pair<String, String> getFlag(String key) {
        try {
            return new RedisGetCommand(rocketMQConsumer.getRedis(), key).execute();
        } catch (Exception e) {
            logger.warn("getFlag:{} error:{}", key, e.toString());
        }
        return null;
    }

    /**
     * 获取offsetMsgId
     * 
     * @param msgExt
     * @return
     */
    private String getOffsetMsgId(MessageExt msgExt) {
        // 延迟消息使用之前的offsetMsgId
        String offsetMsgId = msgExt.getProperty(DELAY_MESSAGE);
        if (offsetMsgId == null) {
            if (msgExt instanceof MessageClientExt) {
                offsetMsgId = ((MessageClientExt) msgExt).getOffsetMsgId();
            }
        }
        return offsetMsgId;
    }

    /**
     * 构建去重的key
     * 
     * @param consumerGroup
     * @param msgExt
     * @return
     */
    private String buildKey(MessageExt msgExt) {
        // 优先使用客户端自己设置的幂等id
        String idempotentId = msgExt.getProperty(MQMessage.IDEMPOTENT_ID);
        if (idempotentId == null) {
            // 其次使用客户端生成的消息id
            idempotentId = MessageClientIDSetter.getUniqID(msgExt);
        }
        if (idempotentId == null) {
            return null;
        }
        return idempotentId;
    }

    /**
     * 发送延迟消息
     * 
     * @param consumerGroup
     * @param msg
     * @return
     */
    @SuppressWarnings("deprecation")
    private boolean sendDelayMessage(String consumerGroup, MessageExt msg) {
        try {
            Message newMsg = new Message(MixAll.getRetryTopic(consumerGroup), msg.getBody());
            String originMsgId = MessageAccessor.getOriginMessageId(msg);
            MessageAccessor.setOriginMessageId(newMsg, UtilAll.isBlank(originMsgId) ? msg.getMsgId() : originMsgId);
            newMsg.setFlag(msg.getFlag());
            MessageAccessor.setProperties(newMsg, msg.getProperties());
            MessageAccessor.putProperty(newMsg, MessageConst.PROPERTY_RETRY_TOPIC, msg.getTopic());
            MessageAccessor.setReconsumeTime(newMsg, String.valueOf(msg.getReconsumeTimes() + 1));
            int delayInterval = (int) (MessageDelayLevel.LEVEL_30_SECONDS.getDelayTimeMillis() / 1000);
            int maxReconsumeTimes = rocketMQConsumer.getDeduplicateWindowSeconds() / delayInterval;
            MessageAccessor.setMaxReconsumeTimes(newMsg, String.valueOf(maxReconsumeTimes));
            MessageAccessor.clearProperty(newMsg, MessageConst.PROPERTY_TRANSACTION_PREPARED);
            newMsg.setDelayTimeLevel(MessageDelayLevel.LEVEL_30_SECONDS.getLevel());
            MessageAccessor.putProperty(newMsg, DELAY_MESSAGE, getOffsetMsgId(msg));
            SendResult sendResult = rocketMQConsumer.getConsumer().getDefaultMQPushConsumerImpl().getmQClientFactory()
                    .getDefaultMQProducer().send(newMsg);
            logger.info(
                    "sendDelayMessage consumerGroup:{} msgId:{} offsetMsgId:{} reconsumeTimes:{} maxReconsumeTimes:{} result:{}",
                    consumerGroup, msg.getMsgId(), getOffsetMsgId(msg), msg.getReconsumeTimes(), maxReconsumeTimes,
                    sendResult);
            return true;
        } catch (Exception e) {
            logger.warn("sendDelayMessage consumerGroup:{} msgId:{} offsetMsgId:{} reconsumeTimes:{} error:{}",
                    msg.getMsgId(), getOffsetMsgId(msg), msg.getReconsumeTimes(), e.toString());
        }
        return false;
    }
}
