package com.sohu.tv.mq.stats;

import java.util.Map;
/**
 * mbean
 * 
 * @author yongfeigao
 * @date 2018年9月19日
 */
public interface StatsHelperMBean {
    /**
     * 采样结果统计
     * @return
     */
    public Map<String, String> getSampleStats();
    
    /**
     * 上报结果统计
     * @return
     */
    public Map<String, String> getReportStats();
    
    /**
     * 调用统计-最大值，平均值等
     * @return
     */
    public Map<String, Object> getInvokeStats();
    
    /**
     * 调用统计-时间段，百分位数
     * @return
     */
    public Map<String, Object> getTimeSectionStats();
    
    /**
     * 停止统计
     */
    public void stop();
    
    /**
     * 开启统计
     */
    public void start();
    
    /**
     * 是否开启
     * @return
     */
    public boolean isStoped();
}
