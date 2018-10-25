package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * 消费者参数
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月17日
 */
public class ConsumerParam {
    // topic id
    @Range(min = 1)
    private long tid;
    // consumer name
    @NotBlank
    private String consumer;
    // 消费方式 0:集群消费,1:广播消费
    @Range(min = 0, max = 1)
    private int consumeWay;
    //描述
    private String info;
    
    public long getTid() {
        return tid;
    }
    public void setTid(long tid) {
        this.tid = tid;
    }
    public String getConsumer() {
        return consumer;
    }
    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }
    public int getConsumeWay() {
        return consumeWay;
    }
    public void setConsumeWay(int consumeWay) {
        this.consumeWay = consumeWay;
    }
    
    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }
    @Override
    public String toString() {
        return "ConsumerParam [tid=" + tid + ", consumer=" + consumer + ", consumeWay=" + consumeWay + "]";
    }
}
