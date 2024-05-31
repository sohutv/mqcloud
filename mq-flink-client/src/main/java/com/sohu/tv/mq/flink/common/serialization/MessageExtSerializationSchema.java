package com.sohu.tv.mq.flink.common.serialization;

import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.io.Serializable;

/**
 * @project mqcloud
 * @description
 * @author fengwang219475
 * @date 2024/5/22 09:17:40
 * @version 1.0
 */
public interface MessageExtSerializationSchema <T> extends ResultTypeQueryable<T>, Serializable {

    byte[] serializeMessage(T t) throws Exception;
}
