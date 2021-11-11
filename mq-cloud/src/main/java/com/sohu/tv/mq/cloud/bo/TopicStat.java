package com.sohu.tv.mq.cloud.bo;

/**
 * topic状况
 * 
 * @author yongfeigao
 * @date 2021年8月24日
 */
public class TopicStat {
    // topic数量
    private int size;
    // topic流量
    private int count;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
