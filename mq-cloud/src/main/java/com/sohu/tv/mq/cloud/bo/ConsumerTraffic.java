package com.sohu.tv.mq.cloud.bo;

/**
 * 消费者流量
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月26日
 */
public class ConsumerTraffic extends Traffic {
    
    private long consumerId;

    public long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(long consumerId) {
        this.consumerId = consumerId;
    }

    @Override
    public String toString() {
        return "ConsumerTraffic [consumerId=" + consumerId + ", toString()=" + super.toString() + "]";
    }
}
