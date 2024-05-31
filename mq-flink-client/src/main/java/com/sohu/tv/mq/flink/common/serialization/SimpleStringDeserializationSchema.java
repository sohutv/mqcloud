package com.sohu.tv.mq.flink.common.serialization;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * @project mqcloud
 * @description
 * @author fengwang219475
 * @date 2024/5/17 09:59:21
 * @version 1.0
 */
public class SimpleStringDeserializationSchema implements MessageExtDeserializationSchema<String>{
    
    @Override
    public String deserializeMessageBody(MessageExt k) throws Exception{
        byte[] bytes = k.getBody();
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return new String(bytes, RemotingHelper.DEFAULT_CHARSET);
    }

    @Override
    public TypeInformation<String> getProducedType() {
        return TypeInformation.of(String.class);
    }
}
