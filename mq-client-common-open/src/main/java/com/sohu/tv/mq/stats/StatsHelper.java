package com.sohu.tv.mq.stats;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.rocketmq.common.utils.HttpTinyClient;
import org.apache.rocketmq.common.utils.HttpTinyClient.HttpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.stats.InvokeStats.InvokeStatsResult;
import com.sohu.tv.mq.stats.InvokeStats.TimeAndExceptionStats;
import com.sohu.tv.mq.stats.dto.ClientStats;

/**
 * 统计助手
 * 
 * @author yongfeigao
 * @date 2018年9月11日
 */
public class StatsHelper implements StatsHelperMBean {
    // 客户端host
    private String clientHost;
    // producer
    private String producer;
    // brokerAddr<->调用统计
    private ConcurrentMap<String, InvokeStats> invokeStatsMap;
    // 时间段统计
    private TimeSectionStats timeSectionStats;
    // 是否停止统计
    private volatile boolean stoped;
    // 状态采样上报
    private StatsReporter statsReporter;

    // mqcloud的域名
    private String mqCloudDomain;

    /**
     * 初始化
     * 
     * @param timeInMillis
     */
    public void init(int timeInMillis) {
        invokeStatsMap = new ConcurrentHashMap<String, InvokeStats>();
        timeSectionStats = new TimeSectionStats(timeInMillis);
        // 初始化上报
        statsReporter = new StatsReporter(this);
        statsReporter.init();
    }

    /**
     * 统计
     * 
     * @param brokerAddr
     * @param timeInMillis
     * @param exception
     */
    public void increment(String brokerAddr, int timeInMillis, Exception exception) {
        // 停止后不再统计
        if (stoped) {
            return;
        }
        // 只取ip
        int idx = brokerAddr.indexOf(":");
        if (idx != -1) {
            brokerAddr = brokerAddr.substring(0, idx);
        }
        // 统计具体时间
        InvokeStats invokeStats = invokeStatsMap.get(brokerAddr);
        if (invokeStats == null) {
            invokeStats = new InvokeStats();
            InvokeStats prev = invokeStatsMap.putIfAbsent(brokerAddr, invokeStats);
            if (prev != null) {
                invokeStats = prev;
            }
        }
        invokeStats.increment(timeInMillis);
        // 统计异常
        if (exception != null) {
            invokeStats.record(exception);
        }
        // 统计时间段
        timeSectionStats.increment(timeInMillis);
    }

    /**
     * 统计报告
     * 
     * @author yongfeigao
     * @date 2018年9月11日
     */
    public static class StatsReporter {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        // 线程数统计
        public static final AtomicInteger threadCounter = new AtomicInteger();

        private StatsHelper statsHelper;

        // 采样统计
        private Stats sampleStats = new Stats();
        // 上报统计
        private Stats reportStats = new Stats();

        public StatsReporter(StatsHelper statsHelper) {
            this.statsHelper = statsHelper;
        }

        /**
         * 采样任务初始化
         */
        public void init() {
            // 数据采样线程
            Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "TimeSectionSamplerScheduledThread-" + threadCounter.incrementAndGet());
                }
            }).scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        report();
                    } catch (Throwable ignored) {
                        logger.warn("report err:{}", ignored.getMessage());
                    }
                }
            }, 60, 60, TimeUnit.SECONDS);
        }

        /**
         * 报告
         */
        public void report() {
            long start = System.currentTimeMillis();
            ConcurrentMap<String, InvokeStats> invokeStatsMap = statsHelper.invokeStatsMap;
            if (invokeStatsMap.size() == 0) {
                sampleStats.recordCost(System.currentTimeMillis() - start, new Date(start));
                return;
            }
            // 百分数采样
            statsHelper.timeSectionStats.sample();
            // 没有调用量不用统计
            if (statsHelper.timeSectionStats.getTotalCount() <= 0) {
                sampleStats.recordCost(System.currentTimeMillis() - start, new Date(start));
                return;
            }
            // 客户端统计结果封装
            ClientStats clientStats = new ClientStats();
            clientStats.setProducer(statsHelper.getProducer());
            clientStats.setClient(statsHelper.getClientHost());

            // 精确统计结果封装
            Map<String, InvokeStatsResult> detailInvoke = new HashMap<String, InvokeStatsResult>();
            for (Entry<String, InvokeStats> entry : invokeStatsMap.entrySet()) {
                InvokeStatsResult invokeStatsResult = entry.getValue().sample();
                if (invokeStatsResult != null) {
                    detailInvoke.put(entry.getKey(), invokeStatsResult);
                }
            }

            // 有数据设置结果
            if (detailInvoke.size() > 0) {
                clientStats.setDetailInvoke(detailInvoke);
            }

            // 百分数结果封装
            clientStats.setStatsTime((int) (System.currentTimeMillis() / 60000));
            clientStats.setAvg(statsHelper.timeSectionStats.avg());
            clientStats.setPercent99(statsHelper.timeSectionStats.percentile(0.99));
            clientStats.setPercent90(statsHelper.timeSectionStats.percentile(0.9));
            clientStats.setCounts(statsHelper.timeSectionStats.getTotalCount());

            // 发送结果
            String stats = JSON.toJSONString(clientStats);
            // 统计采样
            sampleStats.recordCost(System.currentTimeMillis() - start, new Date(start));

            sendToMQCloud(stats);
        }

        /**
         * 发送到MQCloud
         * 
         * @param stats
         */
        private void sendToMQCloud(String stats) {
            long start = System.currentTimeMillis();
            List<String> paramValues = new ArrayList<String>();
            paramValues.add("stats");
            paramValues.add(stats);
            try {
                HttpResult result = HttpTinyClient.httpPost("http://" + statsHelper.getMqCloudDomain() + 
                        "/cluster/report", null, paramValues, "UTF-8", 5000);
                if (HttpURLConnection.HTTP_OK != result.code) {
                    logger.error("http response err: code:{},info:{}", result.code, result.content);
                }
            } catch (Throwable e) {
                logger.error("http err, stats:{}", stats, e);
            }
            // 统计采样
            reportStats.recordCost(System.currentTimeMillis() - start, new Date(start));
        }

        public Stats getSampleStats() {
            return sampleStats;
        }

        public Stats getReportStats() {
            return reportStats;
        }
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    /**
     * 统计
     */
    private static class Stats {
        // 最大耗时
        private long maxCostInMillis;

        // 总耗时
        private long totalCostInMillis;

        // 总次数
        private long totalCount;

        // 上次采样时间
        private Date lastDate;

        /**
         * 统计耗时
         * 
         * @param cost
         */
        public void recordCost(long cost, Date date) {
            ++this.totalCount;
            totalCostInMillis += cost;
            if (maxCostInMillis < cost) {
                maxCostInMillis = cost;
            }
            lastDate = date;
            // 溢出重置
            if (totalCostInMillis < 0) {
                totalCostInMillis = cost;
                totalCount = 1;
            }
        }

        public Map<String, String> getStats() {
            Map<String, String> map = new HashMap<String, String>();
            map.put("maxCostInMillis", String.valueOf(maxCostInMillis));
            map.put("totalCostInMillis", String.valueOf(totalCostInMillis));
            map.put("totalCount", String.valueOf(totalCount));
            if (lastDate != null) {
                map.put("lastDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastDate));
            }
            if (totalCount > 0) {
                map.put("avgCostInMillis", String.valueOf((long) (totalCostInMillis / totalCount * 10) / 10D));
            }
            return map;
        }
    }

    public Map<String, Object> getTimeSectionStats() {
        // 状态map
        Map<String, Object> statsMap = new HashMap<String, Object>();

        // 设置时间段统计
        List<Integer> timeSection = timeSectionStats.getTimeSection();
        AtomicLong[] timeSectionCounter = timeSectionStats.getTimeSectionCounter();
        long[] snapshotData = timeSectionStats.getSnapshotData();
        long[] sampledData = timeSectionStats.getSampledData();
        Map<Integer, Map<String, Long>> timeSectionStatsMap = new TreeMap<Integer, Map<String, Long>>();
        for (int i = 0; i < timeSection.size(); ++i) {
            Map<String, Long> tmpMap = new HashMap<String, Long>();
            tmpMap.put("now", timeSectionCounter[i].get());
            if (snapshotData != null) {
                tmpMap.put("snapshot", snapshotData[i]);
            }
            if (sampledData != null) {
                tmpMap.put("sampled", sampledData[i]);
            }
            timeSectionStatsMap.put(timeSection.get(i), tmpMap);
        }
        statsMap.put("timeSectionStats", timeSectionStatsMap);
        statsMap.put("sampledTotalCount", timeSectionStats.getTotalCount());
        return statsMap;
    }

    @Override
    public void stop() {
        this.stoped = true;
    }

    @Override
    public void start() {
        this.stoped = false;
    }

    public boolean isStoped() {
        return stoped;
    }

    public String getMqCloudDomain() {
        return mqCloudDomain;
    }

    public void setMqCloudDomain(String mqCloudDomain) {
        this.mqCloudDomain = mqCloudDomain;
    }

    public Map<String, String> getSampleStats() {
        return statsReporter.getSampleStats().getStats();
    }

    public Map<String, String> getReportStats() {
        return statsReporter.getReportStats().getStats();
    }

    public Map<String, Object> getInvokeStats() {
        // 设置invoke
        Map<String, Object> invokeMap = new HashMap<String, Object>();
        for (Entry<String, InvokeStats> entry : invokeStatsMap.entrySet()) {
            InvokeStats invokeStats = entry.getValue();
            Map<String, Object> invokeStatsMap = new HashMap<String, Object>();
            int indexer = invokeStats.getIndexer();
            invokeStatsMap.put("indexer", indexer);
            TimeAndExceptionStats timeAndExceptionStats = invokeStats.getStatsArray()[indexer];
            invokeStatsMap.put("timeStats", timeAndExceptionStats.getTimeStats().getMap());
            invokeStatsMap.put("exceptionStats", timeAndExceptionStats.getExceptionStats().getMap());
            invokeMap.put(entry.getKey(), invokeStatsMap);
        }
        return invokeMap;
    }
}
