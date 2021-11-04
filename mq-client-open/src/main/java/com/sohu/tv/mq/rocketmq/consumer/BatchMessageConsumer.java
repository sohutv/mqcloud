package com.sohu.tv.mq.rocketmq.consumer;

import java.util.List;

import org.apache.rocketmq.common.message.MessageExt;

import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.tv.mq.metric.ConsumeStatManager;
import com.sohu.tv.mq.metric.ConsumeThreadStat;
import com.sohu.tv.mq.rocketmq.RocketMQConsumer;

/**
 * 批量消息消费
 * 
 * @author yongfeigao
 * @date 2021年8月31日
 * @param <C>
 */
public class BatchMessageConsumer<C> extends AbstractMessageConsumer<Object, C> {

    public BatchMessageConsumer(RocketMQConsumer rocketMQConsumer) {
        super(rocketMQConsumer);
    }

    @Override
    public ConsumeStatus consume(MessageContext<C> context) {
        List<MQMessage<Object>> msgList = parse(context.msgs);
        if (msgList == null || msgList.isEmpty()) {
            return ConsumeStatus.OK;
        }
        // 设置消费线程统计
        ConsumeThreadStat metric = ConsumeStatManager.getInstance().getConsumeThreadMetrics(rocketMQConsumer.getGroup());
        try {
            metric.set(buildThreadConsumeMetric(msgList));
            // 获取许可
            acquirePermit(msgList.size());
            rocketMQConsumer.getBatchConsumerCallback().call(msgList, context.context);
        } catch (Throwable e) {
            logger.error("topic:{} consumer:{} msgSize:{}", 
                    rocketMQConsumer.getTopic(), rocketMQConsumer.getGroup(), msgList.size(), e);
            ConsumeStatManager.getInstance().getConsumeFailedMetrics(rocketMQConsumer.getGroup())
                    .set(buildMessageExceptionMetric(msgList, e));
            return ConsumeStatus.FAIL;
        } finally {
            metric.remove();
        }
        return ConsumeStatus.OK;
    }

    @Override
    public void consume(Object message, MessageExt msgExt) throws Exception {
    }

}
