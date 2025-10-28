package com.sohu.tv.mq.metric;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.micrometer.core.instrument.Metrics.globalRegistry;

public class MQMetricsMicrometerTest {
    int metricSize = 10;
    int totalMetricSize = metricSize * 4;

    @Test
    public void test() {
        globalRegistry.config().meterFilter(new MaximumAllowableMetrics(1));
        MQMetricsMicrometer mqMetricsMicrometer = new MQMetricsMicrometer();
        collect(mqMetricsMicrometer);
        List<Meter> meters = globalRegistry.getMeters();
        Assert.assertEquals(1, meters.size());
        Assert.assertTrue(mqMetricsMicrometer.getCache().size() == totalMetricSize);
    }

    @Test
    public void testDynamicChange() throws InterruptedException {
        MaximumAllowableMetrics maximumAllowableMetrics = new MaximumAllowableMetrics(1);
        globalRegistry.config().meterFilter(maximumAllowableMetrics);
        MQMetricsMicrometer mqMetricsMicrometer = new MQMetricsMicrometer();
        collect(mqMetricsMicrometer);
        Assert.assertEquals(1, globalRegistry.getMeters().size());
        maximumAllowableMetrics.maximumTimeSeries = totalMetricSize;
        while (globalRegistry.getMeters().size() < totalMetricSize) {
            collect(mqMetricsMicrometer);
            Thread.sleep(1000);
        }
        Assert.assertEquals(mqMetricsMicrometer.getCache().size(), globalRegistry.getMeters().size());
    }

    void collect(MQMetricsMicrometer mqMetricsMicrometer) {
        mqMetricsMicrometer.collect("consumer", getMQMetricsList(metricSize));
    }

    List<MQMetrics> getMQMetricsList(int size) {
        List<MQMetrics> mqMetricsList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            MQMetrics mqMetrics = new MQMetrics();
            mqMetrics.setGroup("testGroup" + i);
            mqMetricsList.add(mqMetrics);
        }
        return mqMetricsList;
    }

    class MaximumAllowableMetrics implements MeterFilter {

        private int maximumTimeSeries;

        public MaximumAllowableMetrics(int maximumTimeSeries) {
            this.maximumTimeSeries = maximumTimeSeries;
        }

        @Override
        public MeterFilterReply accept(Meter.Id id) {
            if (globalRegistry.getMeters().size() >= maximumTimeSeries) {
                return MeterFilterReply.DENY;
            }
            return MeterFilterReply.NEUTRAL;
        }
    }
}