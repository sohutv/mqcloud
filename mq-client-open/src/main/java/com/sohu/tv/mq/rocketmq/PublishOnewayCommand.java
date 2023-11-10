package com.sohu.tv.mq.rocketmq;

import org.apache.rocketmq.client.producer.SendResult;

import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.common.AbstractCommand;
import com.sohu.tv.mq.common.Alerter;
import com.sohu.tv.mq.common.DefaultAlerter;
import com.sohu.tv.mq.common.FallbackException;

/**
 * Producer PublishOneway 隔离封装
 * 
 * @Description: 提供rocketmq PublishOneway方式，隔离发送
 * @author yongfeigao
 * @date 2018年1月24日
 */
@Deprecated
public class PublishOnewayCommand extends AbstractCommand<Result<SendResult>> {

    public static final String GROUP_KEY = "producer";

    public static final String COMMAND_KEY = "publishOneway";

    // mq生产者
    private RocketMQProducer producer;
    // 发送的消息
    private Object messageObject;
    // keys
    private String keys;

    public PublishOnewayCommand(RocketMQProducer producer, Object messageObject) {
        this(producer, messageObject, null);
    }

    public PublishOnewayCommand(RocketMQProducer producer, Object messageObject, String keys) {
        this(producer, messageObject, keys, POOLSIZE, producer.getProducer().getSendMsgTimeout());
    }

    public PublishOnewayCommand(RocketMQProducer producer, Object messageObject, String keys, int poolSize, int timeout) {
        this(producer, messageObject, keys, GROUP_KEY, COMMAND_KEY, poolSize, timeout, DefaultAlerter.getInstance());
    }

    public PublishOnewayCommand(RocketMQProducer producer, Object messageObject, String keys, String groupKey,
            String commandKey, int poolSize, int timeout, Alerter alerter) {
        super(groupKey, commandKey, poolSize, timeout, alerter);
        this.producer = producer;
        this.messageObject = messageObject;
        this.keys = keys;
    }

    protected Result<SendResult> invoke() throws Exception {
        return producer.publishOneway(messageObject, keys);
    }

    protected Object invokeErrorInfo() {
        return "msg:" + messageObject;
    }

    public Result<SendResult> fallback() {
        return new Result<SendResult>(false, new FallbackException());
    }
}
