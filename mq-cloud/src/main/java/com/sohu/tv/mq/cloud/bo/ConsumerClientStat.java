package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * 消费者-客户端统计
 * @author yongweizhao
 * @create 2019/11/6 15:57
 */
public class ConsumerClientStat {
    // consumer
    private String consumer;
    // client
    private String client;
    // 创建时间
    private Date createTime;

    public ConsumerClientStat() {}

    public ConsumerClientStat(String consumer, String client) {
        this.consumer = consumer;
        this.client = client;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ConsumerClientStat{" +
                "consumer='" + consumer + '\'' +
                ", client='" + client + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
