package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * 关联生产者
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月17日
 */
public class AssociateProducerParam {
    // topic id
    @Range(min = 1)
    private long tid;
    // producer
    @NotBlank
    private String producer;
    // 通信协议
    private int protocol;
    public long getTid() {
        return tid;
    }
    public void setTid(long tid) {
        this.tid = tid;
    }
    public String getProducer() {
        return producer;
    }
    public void setProducer(String producer) {
        this.producer = producer;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }
}
