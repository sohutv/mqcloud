package com.sohu.tv.mq.util;

/**
 * 一些常量定义
 * @Description: 
 * @author yongfeigao
 * @date 2018年5月11日
 */
public class Constant {
    /**
     * rocketmq nameserv 域名
     */
    public static final String ROCKETMQ_NAMESRV_DOMAIN = "rocketmq.namesrv.domain";
    
    /**
     * 消费限制tps
     */
    public static final int LIMIT_CONSUME_TPS = 200;
    
    public static final String COMMAND_TRUE = "true";
    
    // 线程统计
    public static final String COMMAND_THREAD_METRIC = "_thread_metric";
    public static final String COMMAND_VALUE_THREAD_METRIC = "threadMetricList";
    
    // 异常统计
    public static final String COMMAND_VALUE_FAILED_METRIC = "failedMetricList";
    public static final String COMMAND_FAILED_METRIC = "_failed_metric";
    
    // 时间段消费
    public static final String COMMAND_TIMESPAN_TOPIC = "_ts_topic";
    public static final String COMMAND_TIMESPAN_START = "_ts_start";
    public static final String COMMAND_TIMESPAN_END = "_ts_end";
}
