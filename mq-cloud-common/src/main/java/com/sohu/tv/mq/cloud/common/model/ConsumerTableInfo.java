package com.sohu.tv.mq.cloud.common.model;

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

import java.util.List;
import java.util.Map;

/**
 * 消费者链接信息
 *
 * @author yongfeigao
 * @date 2024年10月25日
 */
public class ConsumerTableInfo extends RemotingSerializable {
    public ConsumerTableInfo(Map<String, List<ConsumerInfo>> data) {
        this.data = data;
    }

    private Map<String, List<ConsumerInfo>> data;

    public Map<String, List<ConsumerInfo>> getData() {
        return data;
    }

    public void setData(Map<String, List<ConsumerInfo>> data) {
        this.data = data;
    }
}
