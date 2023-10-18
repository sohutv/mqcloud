package com.sohu.tv.mq.cloud.bo;

import java.util.List;

/**
 * 消费者客户端指标
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/27
 */
public class ConsumerClientMetrics {
    // id
    private long id;
    // consumer
    private String consumer;
    // client
    private String client;
    // 最大耗时
    private int max;
    // 平均耗时
    private double avg;
    // 调用次数
    private long count;
    // 异常 格式Map<String<->Integer>;
    private String exception;
    // 统计时间
    private int statTime;
    // 创建日期
    private int createDate;
    // 创建时间
    private String createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public int getStatTime() {
        return statTime;
    }

    public void setStatTime(int statTime) {
        this.statTime = statTime;
    }

    public int getCreateDate() {
        return createDate;
    }

    public void setCreateDate(int createDate) {
        this.createDate = createDate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ConsumerClientMetrics{" +
                "id=" + id +
                ", consumer='" + consumer + '\'' +
                ", client='" + client + '\'' +
                ", max=" + max +
                ", avg=" + avg +
                ", count=" + count +
                ", exception='" + exception + '\'' +
                ", statTime=" + statTime +
                ", createDate=" + createDate +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
