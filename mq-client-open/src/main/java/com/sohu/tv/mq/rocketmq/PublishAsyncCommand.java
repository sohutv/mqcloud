package com.sohu.tv.mq.rocketmq;

import org.apache.rocketmq.client.producer.SendCallback;

import com.sohu.tv.mq.common.AbstractCommand;
import com.sohu.tv.mq.common.Alerter;
import com.sohu.tv.mq.common.DefaultAlerter;

/**
 * Producer PublishAsync 隔离封装
 * 
 * @Description: 提供rocketmq PublishAsync方式，隔离发送
 * 由于SendCallback在rocketmq线程执行，无法把异常抛出到hystrix线程，故隔离机制失效，此类之后将废弃。
 * @author yongfeigao
 * @date 2018年1月24日
 */
@Deprecated
public class PublishAsyncCommand extends AbstractCommand<Void> {

    public static final String GROUP_KEY = "producer";

    public static final String COMMAND_KEY = "PublishAsync";

    // mq生产者
    private RocketMQProducer producer;
    // 发送的消息
    private Object messageObject;
    // keys
    private String keys;
    // 回掉方法
    private SendCallback sendCallback;

    public PublishAsyncCommand(RocketMQProducer producer, Object messageObject, SendCallback sendCallback) {
        this(producer, messageObject, null, sendCallback);
    }

    public PublishAsyncCommand(RocketMQProducer producer, Object messageObject, String keys, SendCallback sendCallback) {
        this(producer, messageObject, keys, POOLSIZE, producer.getProducer().getSendMsgTimeout(), sendCallback);
    }

    public PublishAsyncCommand(RocketMQProducer producer, Object messageObject, String keys, int poolSize, int timeout,
            SendCallback sendCallback) {
        this(producer, messageObject, keys, GROUP_KEY, COMMAND_KEY, poolSize, timeout, DefaultAlerter.getInstance(),
                sendCallback);
    }

    public PublishAsyncCommand(RocketMQProducer producer, Object messageObject, String keys, String groupKey,
            String commandKey, int poolSize, int timeout, Alerter alerter, SendCallback sendCallback) {
        super(groupKey, commandKey, poolSize, timeout, alerter);
        this.producer = producer;
        this.messageObject = messageObject;
        this.keys = keys;
        this.sendCallback = sendCallback;
    }

    protected Void invoke() throws Exception {
        producer.publishAsyncWithException(messageObject, keys, sendCallback);
        return null;
    }

    protected Object invokeErrorInfo() {
        return "msg:" + messageObject;
    }

    public Void fallback() {
        return null;
    }
}
