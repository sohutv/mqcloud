package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.tools.monitor.UndoneMsgs;

/**
 * 具备消费类型的堆积消息
 * 
 * @author yongfeigao
 * @date 2020年8月26日
 */
public class TypedUndoneMsgs extends UndoneMsgs {
    private boolean clustering;

    public boolean isClustering() {
        return clustering;
    }

    public void setClustering(boolean clustering) {
        this.clustering = clustering;
    }

    public void addUndoneMsgsSingleMQ(long undoneMsgsSingleMQ) {
        if (undoneMsgsSingleMQ > 0) {
            setUndoneMsgsTotal(getUndoneMsgsTotal() + undoneMsgsSingleMQ);
        }
        if (undoneMsgsSingleMQ > getUndoneMsgsSingleMQ()) {
            setUndoneMsgsSingleMQ(undoneMsgsSingleMQ);
        }
    }
}
