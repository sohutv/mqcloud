package com.sohu.tv.mq.flink.common.serialization;

import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.rocketmq.common.message.MessageExt;

import java.io.Serializable;

/**
 * @project mqcloud
 * @description
 * @author fengwang219475
 * @date 2024/5/17 09:54:05
 * @version 1.0
 */
public interface MessageExtDeserializationSchema <T> extends ResultTypeQueryable<T>, Serializable {

    T deserializeMessageBody(MessageExt k) throws Exception;
}
