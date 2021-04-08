package com.sohu.tv.mq.metric;

import java.util.Map;

public interface MQMetricsExporterMBean {
    /**
     * 获取生产统计指标
     * 
     * @return
     */
    public Map<String, Map<String, Number>> getProducerMetrics();

    /**
     * 获取消费者统计指标
     * 
     * @return
     */
    public Map<String, Map<String, Number>> getConsumerMetrics();
}
