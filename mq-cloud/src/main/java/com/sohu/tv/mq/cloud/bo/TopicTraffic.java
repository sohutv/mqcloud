package com.sohu.tv.mq.cloud.bo;

/**
 * topic流量
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月26日
 */
public class TopicTraffic extends Traffic {

    private long tid;

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    @Override
    public String toString() {
        return "TopicTraffic [tid=" + tid + ", toString()=" + super.toString() + "]";
    }
}
