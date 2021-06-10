package com.sohu.index.tv.mq.common;

import java.nio.ByteBuffer;

import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;

/**
 * 批量消息
 * 
 * @author yongfeigao
 * @date 2019年10月18日
 * @param <T>
 * @param <MessageExt>
 */
public class MQMessage<T> {
    private T message;
    private MessageExt messageExt;

    public MQMessage(T message, MessageExt messageExt) {
        this.message = message;
        this.messageExt = messageExt;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public MessageExt getMessageExt() {
        return messageExt;
    }

    public void setMessageExt(MessageExt messageExt) {
        this.messageExt = messageExt;
    }
    
    /**
     * 构建offsetMsgId
     * @return
     */
    public String buildOffsetMsgId() {
        int msgIdLength = (messageExt.getSysFlag() & MessageSysFlag.STOREHOSTADDRESS_V6_FLAG) == 0 ? 4 + 4 + 8
                : 16 + 4 + 8;
        ByteBuffer byteBufferMsgId = ByteBuffer.allocate(msgIdLength);
        return MessageDecoder.createMessageId(byteBufferMsgId, messageExt.getStoreHostBytes(),
                messageExt.getCommitLogOffset());
    }
}
