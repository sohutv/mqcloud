package com.sohu.index.tv.mq.common;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageClientExt;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 批量消息
 * 
 * @author yongfeigao
 * @date 2019年10月18日
 * @param <T>
 * @param <MessageExt>
 */
public class MQMessage<T> {
    
    public static final String IDEMPOTENT_ID = "IDEMPOTENT_ID";

    // 发送的原始对象
    private T message;

    // 可以重试的次数
    private int retryTimes = -1;

    // rocketmq 消息
    private Message innerMessage;
    
    // 发送异常，测试用
    private boolean exceptionForTest;

    public MQMessage() {
    }

    public MQMessage(T message, Message innerMessage) {
        this.message = message;
        this.innerMessage = innerMessage;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public MessageExt getMessageExt() {
        return (MessageExt) innerMessage;
    }

    public void setMessageExt(MessageExt messageExt) {
        setInnerMessage(messageExt);
    }

    public Message getInnerMessage() {
        return innerMessage;
    }

    public void setInnerMessage(Message innerMessage) {
        this.innerMessage = innerMessage;
    }

    public MQMessage<T> setKeys(String keys) {
        if (keys != null && keys.length() > 0) {
            innerMessage.setKeys(keys);
        }
        return this;
    }

    public String getKeys() {
        return innerMessage.getKeys();
    }

    public String getTags() {
        return innerMessage.getTags();
    }

    public MQMessage<T> setTags(String tags) {
        innerMessage.setTags(tags);
        return this;
    }

    public MQMessage<T> setDelayTimeLevel(int level) {
        innerMessage.setDelayTimeLevel(level);
        return this;
    }

    public int getDelayTimeLevel() {
        return innerMessage.getDelayTimeLevel();
    }

    public byte[] getBody() {
        return innerMessage.getBody();
    }

    public MQMessage<T> setBody(byte[] body) {
        innerMessage.setBody(body);
        return this;
    }

    public boolean isWaitStoreMsgOK() {
        return innerMessage.isWaitStoreMsgOK();
    }

    public MQMessage<T> setWaitStoreMsgOK(boolean waitStoreMsgOK) {
        innerMessage.setWaitStoreMsgOK(waitStoreMsgOK);
        return this;
    }

    public MQMessage<T> setTopic(String topic) {
        innerMessage.setTopic(topic);
        return this;
    }

    public String getTopic() {
        return innerMessage.getTopic();
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public MQMessage<T> setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public MQMessage<T> resetRetryTimes(int retryTimes) {
        if (this.retryTimes == -1) {
            this.retryTimes = retryTimes;
        }
        return this;
    }

    /**
     * 构建offsetMsgId
     * 
     * @return
     */
    public String buildOffsetMsgId() {
        return innerMessage instanceof MessageClientExt ? ((MessageClientExt)innerMessage).getOffsetMsgId() : null;
    }

    public static <T> MQMessage<T> build(T message) {
        MQMessage<T> mqMessage = new MQMessage<>();
        mqMessage.setMessage(message);
        mqMessage.innerMessage = new Message();
        mqMessage.setWaitStoreMsgOK(true);
        return mqMessage;
    }
    
    /**
     * 设置幂等id
     * 
     * @param idempotentId
     */
    public MQMessage<T> setIdempotentID(String idempotentId) {
        innerMessage.putUserProperty(IDEMPOTENT_ID, idempotentId);
        return this;
    }
    
    public MQMessage<T> setExceptionForTest(boolean exceptionForTest) {
        this.exceptionForTest = exceptionForTest;
        return this;
    }

    public boolean isExceptionForTest() {
        return exceptionForTest;
    }

    @Override
    public String toString() {
        return "[topic=" + getTopic() + ", message=" + message + ", retryTimes=" + retryTimes + "]";
    }
}
