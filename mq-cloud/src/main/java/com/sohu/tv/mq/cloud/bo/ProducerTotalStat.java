package com.sohu.tv.mq.cloud.bo;

import java.util.List;

/**
 * 生产者总体统计
 * 
 * @author yongfeigao
 * @date 2018年9月12日
 */
public class ProducerTotalStat {
    // id
    private long id;
    // producer
    private String producer;
    // client
    private String client;
    // 百分之90
    private int percent90;
    // 百分之99
    private int percent99;
    // 平均耗时
    private double avg;
    // 调用次数
    private long count;
    // 统计时间
    private int statTime;
    // 创建日期
    private int createDate;
    // 创建时间
    private String createTime;
    
    private List<ProducerStat> statList;
    
    // 异常 格式Map<String<->Integer>;
    private String exception;
    // broker
    private String broker;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public int getPercent90() {
        return percent90;
    }

    public void setPercent90(int percent90) {
        this.percent90 = percent90;
    }

    public int getPercent99() {
        return percent99;
    }

    public void setPercent99(int percent99) {
        this.percent99 = percent99;
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

    public List<ProducerStat> getStatList() {
        return statList;
    }

    public void setStatList(List<ProducerStat> statList) {
        this.statList = statList;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    @Override
    public String toString() {
        return "ProducerTotalStat [id=" + id + ", producer=" + producer + ", client=" + client + ", percent90="
                + percent90 + ", percent99=" + percent99 + ", avg=" + avg + ", count=" + count + ", statTime="
                + statTime + ", createDate=" + createDate + ", createTime=" + createTime + ", exception="
                + exception + ", broker=" + broker + "]";
    }
}
