package com.sohu.index.tv.mq.common;

import com.sohu.tv.mq.serializable.MessageSerializer;
import com.sohu.tv.mq.util.Constant;
import com.sohu.tv.mq.util.MQProtocol;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageClientExt;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;

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

    // 是否开启熔断
    private Boolean enableCircuitBreaker;
    // circuitBreaker entryResult
    private Result entryResult;

    // 顺序消息参数
    private Object orderArg;

    // 异步发送回调
    private SendCallback sendCallback;

    // 是否单向发送
    private boolean oneWay;

    // 是否事务消息
    private boolean transaction;

    // 事务消息回调参数
    private Object transactionArg;

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
        if (tags != null && tags.length() > 0) {
            innerMessage.setTags(tags);
        }
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

    public boolean needRetry(Exception e) {
        return retryTimes > 0 && isRemoteError(e);
    }

    /**
     * 重置重试次数，未配置时采用默认值
     */
    public MQMessage<T> resetRetryTimes(int retryTimes) {
        if (this.retryTimes == -1) {
            this.retryTimes = retryTimes;
        }
        return this;
    }

    /**
     * 重置是否开启熔断，未配置时采用默认值
     */
    public MQMessage<T> resetEnableCircuitBreaker(boolean enableCircuitBreaker) {
        if (this.enableCircuitBreaker == null) {
            this.enableCircuitBreaker = enableCircuitBreaker;
        }
        return this;
    }

    public boolean isEnableCircuitBreaker() {
        return enableCircuitBreaker == null ? false : enableCircuitBreaker;
    }

    public void setEnableCircuitBreaker(Boolean enableCircuitBreaker) {
        this.enableCircuitBreaker = enableCircuitBreaker;
    }

    public Result getEntryResult() {
        return entryResult;
    }

    public void setEntryResult(Result entryResult) {
        this.entryResult = entryResult;
    }

    public boolean isEntryOK() {
        return entryResult != null && entryResult.isSuccess();
    }

    public boolean isEntryFailed() {
        return entryResult != null && !entryResult.isSuccess();
    }

    /**
     * 是否是远程错误
     */
    public boolean isRemoteError(Throwable e) {
        if (e instanceof MQClientException && e.getCause() != null) {
            e = e.getCause();
        }
        return e instanceof RemotingException || e instanceof MQBrokerException;
    }

    /**
     * 构建offsetMsgId
     * 
     * @return
     */
    public String buildOffsetMsgId() {
        return innerMessage instanceof MessageClientExt ? ((MessageClientExt)innerMessage).getOffsetMsgId() : null;
    }

    public String getMsgId() {
        String msgId = buildOffsetMsgId();
        if (msgId == null) {
            msgId = getMessageExt().getMsgId();
        }
        return msgId;
    }

    public static <T> MQMessage<T> build(T message) {
        MQMessage<T> mqMessage = new MQMessage<>();
        if (message instanceof Message) {
            mqMessage.innerMessage = (Message) message;
        } else if (message instanceof byte[]) {
            mqMessage.innerMessage = new Message();
            mqMessage.setBody((byte[]) message);
        } else {
            mqMessage.setMessage(message);
            mqMessage.innerMessage = new Message();
        }
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

    /**
     * 设置延迟投递的时间戳
     *
     * @param idempotentId
     * @return
     */
    public MQMessage<T> setDeliveryTimestamp(long deliveryTimestamp) {
        innerMessage.setDeliverTimeMs(deliveryTimestamp);
        return this;
    }
    
    public MQMessage<T> setExceptionForTest(boolean exceptionForTest) {
        this.exceptionForTest = exceptionForTest;
        return this;
    }

    public boolean isExceptionForTest() {
        return exceptionForTest;
    }

    public MQMessage<T> setOrderArg(Object orderArg) {
        this.orderArg = orderArg;
        return this;
    }

    public Object getOrderArg() {
        return orderArg;
    }

    public MQMessage<T> setSendCallback(SendCallback sendCallback) {
        this.sendCallback = sendCallback;
        return this;
    }

    public SendCallback getSendCallback() {
        return sendCallback;
    }

    public MQMessage<T> setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
        return this;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public MQMessage<T> setTransaction(boolean transaction) {
        this.transaction = transaction;
        return this;
    }

    public boolean isTransaction() {
        return transaction;
    }

    public MQMessage<T> setTransactionArg(Object transactionArg) {
        this.transactionArg = transactionArg;
        return this;
    }

    public Object getTransactionArg() {
        return transactionArg;
    }

    public MQMessage<T> setClientHost(String clientHost) {
        if (clientHost != null && clientHost.length() > 0 && clientHost.length() < 16) {
            innerMessage.getProperties().put(MessageConst.PROPERTY_BORN_HOST, clientHost);
        }
        return this;
    }

    public MQMessage<T> serialize(MessageSerializer messageSerializer) throws Exception {
        if (innerMessage.getBody() == null) {
            innerMessage.setBody(messageSerializer.serialize(message));
        }
        return this;
    }

    @Override
    public String toString() {
        return "[topic=" + getTopic() + ", message=" + message + ", retryTimes=" + retryTimes + "]";
    }
}
