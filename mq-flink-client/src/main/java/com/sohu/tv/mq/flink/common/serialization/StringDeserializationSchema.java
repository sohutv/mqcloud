package com.sohu.tv.mq.flink.common.serialization;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * @project mqcloud
 * @description
 * @author fengwang219475
 * @date 2024/5/17 09:59:21
 * @version 1.0
 */
public class StringDeserializationSchema implements MessageExtDeserializationSchema<Tuple2<String, MessageExt>> {
    
    @Override
    public Tuple2<String, MessageExt> deserializeMessageBody(MessageExt k) throws Exception{
        byte[] bytes = k.getBody();
        if (bytes == null || bytes.length == 0) {
            return Tuple2.of(null, k);
        }
        return Tuple2.of(new String(bytes, RemotingHelper.DEFAULT_CHARSET), k);
    }

    @Override
    public TypeInformation<Tuple2<String, MessageExt>> getProducedType() {
        return TypeInformation.of(new TypeHint<Tuple2<String, MessageExt>>() {});
    }
}
