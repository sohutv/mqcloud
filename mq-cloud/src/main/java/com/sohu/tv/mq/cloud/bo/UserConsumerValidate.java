package com.sohu.tv.mq.cloud.bo;

/**
 * 用户消费者校验对象
 * 
 * @Description:
 * @author 元哲宏
 * @date 2018年9月11日
 */
public class UserConsumerValidate extends UserTopic {
    private String consumer;

    @Override
    public String toString() {
        return "UserConsumer [consumer=" + consumer + ", toString()=" + super.toString() + "]";
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

}
