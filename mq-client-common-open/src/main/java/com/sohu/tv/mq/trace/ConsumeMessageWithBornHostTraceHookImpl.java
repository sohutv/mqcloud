package com.sohu.tv.mq.trace;

import org.apache.rocketmq.client.hook.ConsumeMessageContext;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.common.message.MessageConst;

import java.util.HashMap;
import java.util.Map;

/**
 * 消费消息Trace时记录bornHost
 */
public class ConsumeMessageWithBornHostTraceHookImpl implements ConsumeMessageHook {


    @Override
    public String hookName() {
        return "ConsumeMessageWithBornHostTraceHook";
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        if (context == null || context.getMsgList() == null || context.getMsgList().isEmpty()) {
            return;
        }
        String bornHost = ClientHostThreadLocal.get();
        if (bornHost == null) {
            return;
        }
        Map<String, String> props = context.getProps();
        if (props == null) {
            props = new HashMap<>();
            context.setProps(props);
        }
        props.put(MessageConst.PROPERTY_BORN_HOST, bornHost);
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
    }
}
