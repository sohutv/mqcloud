package com.sohu.tv.mq.metric;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * rocketmq指标统计
 *
 * @author yongfeigao
 * @date 2020年12月24日
 */
public class MQMetricsMicrometer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, GaugeCache> cache = new HashMap<>();

    private long lastCheckTime = System.currentTimeMillis();

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
                    checkMetricsRegister();
                } catch (Throwable ignored) {
                    logger.warn("collect err:{}", ignored.getMessage());
                }
            }
        }, 3000, 30000, TimeUnit.MILLISECONDS);
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

    public void collect(String role, List<MQMetrics> mqMetricsList) {
        if (mqMetricsList.isEmpty()) {
            return;
        }
        for (MQMetrics mqMetrics : mqMetricsList) {
            String group = mqMetrics.getGroup();
            get("rocketmq_max_time", role, group).set(mqMetrics.getMaxTime());
            get("rocketmq_total_time", role, group).set(mqMetrics.getTotalTime());
            get("rocketmq_total_count", role, group).set(mqMetrics.getTotalCount());
            get("rocketmq_excption_count", role, group).set(mqMetrics.getExceptionCount());
        }
    }

    public GaugeCache get(String name, String role, String group) {
        String key = name + ":" + role + ":" + group;
        return cache.computeIfAbsent(key, k -> {
            AtomicLong value = new AtomicLong();
            Gauge gauge = Gauge.builder(name, value, AtomicLong::get)
                    .tag("role", role)
                    .tag("group", group)
                    .register(Metrics.globalRegistry);
            return new GaugeCache(value, gauge);
        });
    }

    public Map<String, GaugeCache> getCache() {
        return cache;
    }

    /**
     * 检查指标注册情况，删除已经被删除的指标
     */
    public void checkMetricsRegister() {
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < 5 * 60 * 1000) {
            return;
        }
        lastCheckTime = now;
        // 获取所有gauge指标
        Set<Id> ids = new HashSet<>();
        List<Meter> meters = Metrics.globalRegistry.getMeters();
        for (Meter meter : meters) {
            if (meter.getId().getType() == Type.GAUGE) {
                ids.add(meter.getId());
            }
        }
        int metricSize = meters.size();
        // 删除已经被删除的指标
        cache.entrySet().removeIf(e -> {
            Id id = e.getValue().gauge.getId();
            if (!ids.contains(id)) {
                logger.warn("metricSize:{} remove gauge:{}", metricSize, id);
                return true;
            }
            return false;
        });
    }

    class GaugeCache {

        private AtomicLong value;

        private Gauge gauge;

        public GaugeCache(AtomicLong value, Gauge gauge) {
            this.value = value;
            this.gauge = gauge;
        }

        public void set(long v) {
            value.set(v);
        }
    }
}
