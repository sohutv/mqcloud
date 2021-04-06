package com.sohu.tv.mq.stats.dto;

import java.util.Map;

import com.sohu.tv.mq.stats.InvokeStats.InvokeStatsResult;

/**
 * 客户端统计
 * 
 * @author yongfeigao
 * @date 2018年9月11日
 */
public class ClientStats {
    // 客户端
    private String client;
    // producer
    private String producer;
    // 99%调用耗时在percent99以下
    private int percent99;
    // 90%调用耗时在percent90以下
    private int percent90;
    // 平均耗时
    private double avg;
    // 总调用次数
    private long counts;
    // brokerAddr<->调用统计
    private Map<String, InvokeStatsResult> detailInvoke;
    // 统计时间 需要*60000，才是毫秒
    private int statsTime;
    // 总耗时
    private long totalTime;
    
    // 异常集合
    private Map<String, Object> exceptionMap;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public int getPercent99() {
        return percent99;
    }

    public void setPercent99(int percent99) {
        this.percent99 = percent99;
    }

    public int getPercent90() {
        return percent90;
    }

    public void setPercent90(int percent90) {
        this.percent90 = percent90;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public Map<String, InvokeStatsResult> getDetailInvoke() {
        return detailInvoke;
    }

    public void setDetailInvoke(Map<String, InvokeStatsResult> detailInvoke) {
        this.detailInvoke = detailInvoke;
    }

    public int getStatsTime() {
        return statsTime;
    }

    public void setStatsTime(int statsTime) {
        this.statsTime = statsTime;
    }

    public long getCounts() {
        return counts;
    }

    public void setCounts(long counts) {
        this.counts = counts;
    }

    public Map<String, Object> getExceptionMap() {
        return exceptionMap;
    }

    public void setExceptionMap(Map<String, Object> exceptionMap) {
        this.exceptionMap = exceptionMap;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        return "ClientStats [client=" + client + ", producer=" + producer + ", percent99=" + percent99 + ", percent90="
                + percent90 + ", avg=" + avg + ", counts=" + counts + ", detailInvoke=" + detailInvoke + ", statsTime="
                + statsTime + "]";
    }
}
