package com.sohu.tv.mq.flink.common.serialization;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.rocketmq.common.message.Message;

/**
 * @project mqcloud
 * @description
 * @author fengwang219475
 * @date 2024/5/22 09:22:12
 * @version 1.0
 */
public class SimpleStringSerializationSchema implements MessageExtSerializationSchema<Object>{
    
    @Override
    public byte[] serializeMessage(Object body) throws Exception {
        return JSON.toJSONBytes(body);
    }

    @Override
    public TypeInformation<Object> getProducedType() {
        return TypeInformation.of(Object.class);
    }
}
