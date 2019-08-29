package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * topic参数
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
public class TopicParam {
    // topic name;
    @NotBlank
    private String name;
    // queue num
    @Range(min = 1, max = 20)
    private int queueNum;
    // 是否有序 0:无序,1:有序
    @Range(min = 0, max = 1)
    private int ordered;
    //描述
    private String info;
    // producer name
    @NotBlank
    private String producer;
    
    // 是否启用trace 0:不启用,1:启用
    @Range(min = 0, max = 1)
    private int traceEnabled;
    
    // 是否启用transaction 0:不启用,1:启用
    private int transactionEnabled;
    
    private long qpd;
    private long qps;
    
    // 是否测试topic
    @Range(min = 0, max = 1)
    private int testEnabled;
    
    // 是否消息延迟
    @Range(min = 0, max = 1)
    private int delayEnabled;
    
    // 序列化器
    @Range(min = 0, max = 1)
    private int serializer;
    
    public String getProducer() {
        return producer;
    }
    public void setProducer(String producer) {
        this.producer = producer;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getQueueNum() {
        return queueNum;
    }
    public void setQueueNum(int queueNum) {
        this.queueNum = queueNum;
    }
    public int getOrdered() {
        return ordered;
    }
    public void setOrdered(int ordered) {
        this.ordered = ordered;
    }
    
    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }
  
    public long getQpd() {
        return qpd;
    }
    public void setQpd(long qpd) {
        this.qpd = qpd;
    }
    public long getQps() {
        return qps;
    }
    public void setQps(long qps) {
        this.qps = qps;
    }
    public int getTraceEnabled() {
        return traceEnabled;
    }
    public void setTraceEnabled(int traceEnabled) {
        this.traceEnabled = traceEnabled;
    }
    public int getTransactionEnabled() {
        return transactionEnabled;
    }
    public void setTransactionEnabled(int transactionEnabled) {
        this.transactionEnabled = transactionEnabled;
    }
    public int getTestEnabled() {
        return testEnabled;
    }
    public void setTestEnabled(int testEnabled) {
        this.testEnabled = testEnabled;
    }
    public int getDelayEnabled() {
        return delayEnabled;
    }
    public void setDelayEnabled(int delayEnabled) {
        this.delayEnabled = delayEnabled;
    }
    public int getSerializer() {
        return serializer;
    }
    public void setSerializer(int serializer) {
        this.serializer = serializer;
    }
    @Override
    public String toString() {
        return "TopicParam [name=" + name + ", queueNum=" + queueNum + ", ordered=" + ordered + ", info=" + info
                + ", producer=" + producer + ", traceEnabled=" + traceEnabled + ", transactionEnabled="
                + transactionEnabled + ", qpd=" + qpd + ", qps=" + qps + ", testEnabled=" + testEnabled
                + ", delayEnabled=" + delayEnabled + ", serializer=" + serializer + "]";
    }
}
