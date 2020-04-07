package com.sohu.tv.mq.common;

import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.trace.TraceContext;
import org.apache.rocketmq.common.UtilAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.mq.stats.StatsHelper;

/**
 * 对发送消息进行hook
 * 
 * @author yongfeigao
 * @date 2018年9月5日
 */
public class SohuSendMessageHook implements SendMessageHook {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 统计助手
    private StatsHelper statsHelper;

    public SohuSendMessageHook(DefaultMQProducer producer) {
        statsHelper = new StatsHelper();
        // 最大耗时，延后500毫秒
        statsHelper.init(producer.getSendMsgTimeout() + 500);
        // 客户端id
        statsHelper.setClientId(buildMQClientId(producer));
        // 获取生产者group
        statsHelper.setProducer(producer.getProducerGroup());
    }

    @Override
    public String hookName() {
        return "sohu";
    }

    @Override
    public void sendMessageBefore(SendMessageContext context) {
        if (context.getMqTraceContext() == null) {
            context.setMqTraceContext(System.currentTimeMillis());
        }
    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {
        Object obj = context.getMqTraceContext();
        if (obj == null) {
            return;
        }
        long start = 0;
        // 兼容4.4的trace对象
        if (obj instanceof TraceContext) {
            start = ((TraceContext) obj).getTimeStamp();
        } else if (obj instanceof Long) {
            start = (Long) obj;
        } else {
            return;
        }
        long cost = System.currentTimeMillis() - start;
        try {
            statsHelper.increment(context.getBrokerAddr(), (int) cost, context.getException());
        } catch (Throwable e) {
            logger.warn("stats err", e);
        }
    }

    /**
     * copy from org.apache.rocketmq.client.ClientConfig.buildMQClientId()
     * 将changeInstanceNameToPID逻辑加了进来
     * @param producer
     * @return
     */
    private String buildMQClientId(DefaultMQProducer producer) {
        StringBuilder sb = new StringBuilder();
        sb.append(producer.getClientIP());
        sb.append("@");
        if (producer.getInstanceName().equals("DEFAULT")) {
            sb.append(String.valueOf(UtilAll.getPid()));
        } else {
            sb.append(producer.getInstanceName());
        }
        if (!UtilAll.isBlank(producer.getUnitName())) {
            sb.append("@");
            sb.append(producer.getUnitName());
        }
        return sb.toString();
    }

    public StatsHelper getStatsHelper() {
        return statsHelper;
    }
}
