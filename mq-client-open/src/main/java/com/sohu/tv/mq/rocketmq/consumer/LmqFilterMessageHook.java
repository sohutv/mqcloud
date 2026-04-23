package com.sohu.tv.mq.rocketmq.consumer;

import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.client.hook.FilterMessageContext;
import org.apache.rocketmq.client.hook.FilterMessageHook;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

public class LmqFilterMessageHook implements FilterMessageHook {

    private String topic;

    public LmqFilterMessageHook(String topic) {
        this.topic = topic;
    }

    @Override
    public String hookName() {
        return getClass().getName();
    }

    @Override
    public void filterMessage(FilterMessageContext context) {
        List<MessageExt> msgList = context.getMsgList();
        if (msgList == null) {
            return;
        }
        for (MessageExt msg : msgList) {
            msg.setQueueOffset(CommonUtil.getLmqOffset(topic, msg));
        }
    }
}
