package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.Range;

/**
 * 关联消费者
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月17日
 */
public class AssociateConsumerParam {
    // topic id
    @Range(min = 1)
    private long tid;
    // consumer id
    @Range(min = 1)
    private long cid;
    public long getTid() {
        return tid;
    }
    public void setTid(long tid) {
        this.tid = tid;
    }
    public long getCid() {
        return cid;
    }
    public void setCid(long cid) {
        this.cid = cid;
    }
}
