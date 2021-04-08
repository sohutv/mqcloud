package com.sohu.tv.mq.metric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;

/**
 * rocketmq指标统计
 * 
 * @author yongfeigao
 * @date 2020年12月24日
 */
public class MQMetricsMicrometer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // {role:{group:{metrics:AtomicLong}}}
    private Map<String, Map<String, Map<String, AtomicLong>>> metricMap = new HashMap<>();

    public MQMetricsMicrometer() {
        // 指标收集线程
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "MQMetricsMicrometer");
            }
        }).scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    collect();
                } catch (Throwable ignored) {
                    logger.warn("collect err:{}", ignored.getMessage());
                }
            }
        }, 60000, 30000, TimeUnit.MILLISECONDS);
    }

    /**
     * 收集数据
     */
    public void collect() {
        try {
            List<MQMetrics> producerMetricsList = MQMetricsExporter.getInstance().getProducerMetricsList();
            collect("producer", producerMetricsList);
            List<MQMetrics> consumerMetricsList = MQMetricsExporter.getInstance().getConsumerMetricsList();
            collect("consumer", consumerMetricsList);
        } catch (Throwable e) {
            logger.warn("mbean getAttribute error:{}", e.getMessage());
        }
    }

    /**
     * 收集
     * 
     * @param role
     * @param dataMap
     */
    public void collect(String role, List<MQMetrics> mqMetricsList) {
        if (mqMetricsList.size() == 0) {
            return;
        }
        // 获取角色map
        Map<String, Map<String, AtomicLong>> groupMap = metricMap.computeIfAbsent(role, key -> new HashMap<>());
        for (MQMetrics mqMetrics : mqMetricsList) {
            String group = mqMetrics.getGroup();
            // 获取指标map
            Map<String, AtomicLong> itemMap = groupMap.computeIfAbsent(group, key -> new HashMap<>());
            // 设置最大耗时
            itemMap.computeIfAbsent("rocketmq_max_time", key -> getCounter(key, role, group))
                    .set(mqMetrics.getMaxTime());
            // 设置总耗时
            itemMap.computeIfAbsent("rocketmq_total_time", key -> getCounter(key, role, group))
                    .set(mqMetrics.getTotalTime());
            // 设置调用量
            itemMap.computeIfAbsent("rocketmq_total_count", key -> getCounter(key, role, group))
                    .set(mqMetrics.getTotalCount());
            // 设置异常量
            itemMap.computeIfAbsent("rocketmq_excption_count", key -> getCounter(key, role, group))
                    .set(mqMetrics.getExceptionCount());
        }
    }

    private AtomicLong getCounter(String key, String role, String group) {
        return Metrics.gauge(key, Tags.of("role", role, "group", group), new AtomicLong());
    }
}
