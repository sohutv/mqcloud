package com.sohu.tv.mq.metric;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.mq.stats.ConsumeStats;
import com.sohu.tv.mq.stats.InvokeStats.InvokeStatsResult;
import com.sohu.tv.mq.stats.StatsHelper;
import com.sohu.tv.mq.stats.dto.ClientStats;

/**
 * 指标工具
 * 
 * @author yongfeigao
 * @date 2020年12月21日
 */
public class MQMetricsExporter implements MQMetricsExporterMBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String MBEAN_NAME = "com.sohu.tv.mq:name=mqMetrics";

    private List<StatsHelper> statsHelperList = new LinkedList<>();

    private List<ConsumeStats> consumeStatsList = new LinkedList<>();
    
    private static boolean canMetrics;
    
    MQMetricsMicrometer mqMetricsMicrometer;

    static {
        try {
            canMetrics = null != Class.forName("io.micrometer.core.instrument.Metrics");
        } catch (Throwable t) {
            // ignore
        }
    }

    private static final MQMetricsExporter _instance = new MQMetricsExporter();
    
    private MQMetricsExporter() {
        if (canMetrics) {
            mqMetricsMicrometer = new MQMetricsMicrometer();
            logger.info("MQMetricsMicrometer init");
        }
    }

    public static MQMetricsExporter getInstance() {
        return _instance;
    }

    public void add(StatsHelper statsHelper) {
        statsHelperList.add(statsHelper);
        registerMBean();
    }

    public void add(ConsumeStats consumeStats) {
        consumeStatsList.add(consumeStats);
        registerMBean();
    }

    /**
     * 获取生产统计指标
     * 
     * @return
     */
    public List<MQMetrics> getProducerMetricsList() {
        List<MQMetrics> list = new LinkedList<>();
        for (StatsHelper statsHelper : statsHelperList) {
            ClientStats clientStats = statsHelper.getClientStats();
            MQMetrics mqMetrics = toMQMetrics(clientStats);
            mqMetrics.setGroup(statsHelper.getProducer());
            list.add(mqMetrics);
        }
        return list;
    }

    /**
     * 获取消费者统计指标
     * 
     * @return
     */
    public List<MQMetrics> getConsumerMetricsList() {
        List<MQMetrics> list = new LinkedList<>();
        for (ConsumeStats consumeStats : consumeStatsList) {
            MQMetrics mqMetrics = toMQMetrics(consumeStats.getInvokeStatsResult());
            mqMetrics.setGroup(consumeStats.getConsumer());
            list.add(mqMetrics);
        }
        return list;
    }

    /**
     * 转换为MQMetrics
     * 
     * @param clientStats
     * @return
     */
    private MQMetrics toMQMetrics(ClientStats clientStats) {
        MQMetrics mqMetrics = new MQMetrics();
        if (clientStats == null) {
            return mqMetrics;
        }
        Map<String, InvokeStatsResult> map = clientStats.getDetailInvoke();
        if (map == null || map.size() == 0) {
            return mqMetrics;
        }
        for (InvokeStatsResult invokeStatsResult : map.values()) {
            addToMQMetrics(mqMetrics, invokeStatsResult);
        }
        return mqMetrics;
    }

    /**
     * 转换为MQMetrics
     * 
     * @param clientStats
     * @return
     */
    private MQMetrics toMQMetrics(InvokeStatsResult invokeStatsResult) {
        MQMetrics mqMetrics = new MQMetrics();
        if (invokeStatsResult == null) {
            return mqMetrics;
        }
        addToMQMetrics(mqMetrics, invokeStatsResult);
        return mqMetrics;
    }

    private void addToMQMetrics(MQMetrics mqMetrics, InvokeStatsResult invokeStatsResult) {
        mqMetrics.setMaxTime(invokeStatsResult.getMaxTime())
                .addTotalCount(invokeStatsResult.getTimes())
                .addTotalTime(invokeStatsResult.totalTime());
        if (invokeStatsResult.getExceptionMap() != null) {
            for (Integer count : invokeStatsResult.getExceptionMap().values()) {
                mqMetrics.addExceptionCount(count);
            }
        }
    }

    private void registerMBean() {
        try {
            ObjectName objectName = new ObjectName(MBEAN_NAME);
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            if (!mBeanServer.isRegistered(objectName)) {
                logger.info("register mbean:{}", MBEAN_NAME);
                mBeanServer.registerMBean(this, objectName);
            }
        } catch (Throwable e) {
            logger.warn("mqmetrics mbean register error:{}", e.getMessage());
        }
    }

    @Override
    public Map<String, Map<String, Number>> getProducerMetrics() {
        return toMap(getProducerMetricsList());
    }

    @Override
    public Map<String, Map<String, Number>> getConsumerMetrics() {
        return toMap(getConsumerMetricsList());
    }
    
    private Map<String, Map<String, Number>> toMap(List<MQMetrics> list) {
        if (list.size() == 0) {
            return null;
        }
        Map<String, Map<String, Number>> map = new HashMap<>();
        for (MQMetrics mqMetrics : list) {
            Map<String, Number> metricsMap = map.get(mqMetrics.getGroup());
            if (metricsMap == null) {
                metricsMap = new HashMap<>();
                map.put(mqMetrics.getGroup(), metricsMap);
            }
            metricsMap.put("maxTime", mqMetrics.getMaxTime());
            metricsMap.put("totalTime", mqMetrics.getTotalTime());
            metricsMap.put("totalCount", mqMetrics.getTotalCount());
            metricsMap.put("exceptionCount", mqMetrics.getExceptionCount());
        }
        return map;
    }
}
