package com.sohu.tv.mq.trace;

import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.trace.TraceBean;
import org.apache.rocketmq.client.trace.TraceContext;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageBatch;
import org.apache.rocketmq.common.message.MessageConst;

/**
 * 发送消息Trace时记录bornHost
 */
public class SendMessageWithBornHostTraceHookImpl implements SendMessageHook {

    @Override
    public String hookName() {
        return "SendMessageWithBornHostTraceHook";
    }

    @Override
    public void sendMessageBefore(SendMessageContext context) {
        TraceContext traceContext = (TraceContext) context.getMqTraceContext();
        if (traceContext == null || traceContext.getTraceBeans().isEmpty()) {
            return;
        }
        Message message = context.getMessage();
        if (message instanceof MessageBatch) {
            message = ((MessageBatch) message).iterator().next();
        }
        String bornHost = message.getUserProperty(MessageConst.PROPERTY_BORN_HOST);
        if (bornHost != null) {
            TraceBean traceBean = traceContext.getTraceBeans().get(0);
            traceBean.setClientHost(bornHost);
        }
    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {
    }
}
