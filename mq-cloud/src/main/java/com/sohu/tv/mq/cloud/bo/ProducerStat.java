package com.sohu.tv.mq.cloud.bo;


import com.sohu.tv.mq.util.JSONUtil;

import java.util.Map;

/**
 * 生产者统计
 * 
 * @author yongfeigao
 * @date 2018年9月12日
 */
public class ProducerStat {
    // id
    private long totalId;
    // broker
    private String broker;
    // 百分之90
    private int max;
    // 平均耗时
    private double avg;
    // 调用次数
    private int count;
    // 异常 格式Map<String<->Integer>;
    private String exception;
    
    // 创建时间
    private String createTime;

    public long getTotalId() {
        return totalId;
    }

    public void setTotalId(long totalId) {
        this.totalId = totalId;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getException() {
        return exception;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Integer> getExceptionJson() {
        if(exception == null) {
            return null;
        }
        return JSONUtil.parse(exception, Map.class);
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "ProducerStat [totalId=" + totalId + ", broker=" + broker + ", max=" + max + ", avg=" + avg + ", count="
                + count + ", exception=" + exception + "]";
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
