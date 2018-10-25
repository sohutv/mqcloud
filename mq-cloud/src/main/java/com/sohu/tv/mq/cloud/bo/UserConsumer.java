package com.sohu.tv.mq.cloud.bo;

/**
 * 用户消费者对象
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
public class UserConsumer extends UserTopic {
    private long consumerId;

    public long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(long consumerId) {
        this.consumerId = consumerId;
    }

    @Override
    public String toString() {
        return "UserConsumer [consumerId=" + consumerId + ", toString()=" + super.toString() + "]";
    }
    
}
