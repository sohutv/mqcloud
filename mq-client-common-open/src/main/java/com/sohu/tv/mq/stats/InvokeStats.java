package com.sohu.tv.mq.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 调用统计
 * 
 * @author yongfeigao
 * @date 2018年9月11日
 */
public class InvokeStats {

    // 统计数组，只有两个元素，来回切换使用
    private TimeAndExceptionStats[] statsArray = new TimeAndExceptionStats[2];

    // 当前统计的下标
    private volatile int indexer;
    
    public InvokeStats() {
        for(int i = 0; i < statsArray.length; ++i) {
            statsArray[i] = new TimeAndExceptionStats();
        }
    }

    /**
     * 记录耗时
     * 
     * @param timeInMillis
     */
    public void increment(long timeInMillis) {
        statsArray[indexer].getTimeStats().increment(timeInMillis);
    }

    /**
     * 记录异常
     * 
     * @param timeInMillis
     */
    public void record(Exception exception) {
        statsArray[indexer].getExceptionStats().record(exception);
    }

    /**
     * 数据采样
     * 
     * @return
     */
    public InvokeStatsResult sample() {
        // 记录当前索引
        int index = indexer;
        // 当前索引切换
        indexer = (indexer + 1) % statsArray.length;
        // 获取统计耗时
        TimeStats timeStats = statsArray[index].getTimeStats();
        // 没有调用
        if(timeStats.getCount().get() <= 0) {
            return null;
        }
        // 获取统计异常
        ExceptionStats exceptionStats = statsArray[index].getExceptionStats();
        // 封装返回结果
        InvokeStatsResult invokeStatsResult = new InvokeStatsResult();
        invokeStatsResult.init(timeStats);
        invokeStatsResult.init(exceptionStats);
        // 重置，以便下次使用
        timeStats.reset();
        // 重置，以便下次使用
        exceptionStats.reset();
        return invokeStatsResult;
    }

    public TimeAndExceptionStats[] getStatsArray() {
        return statsArray;
    }

    public int getIndexer() {
        return indexer;
    }

    /**
     * 时间和异常统计
     * 
     * @author yongfeigao
     * @date 2018年9月11日
     */
    public class TimeAndExceptionStats {
        private TimeStats timeStats = new TimeStats();
        private ExceptionStats exceptionStats = new ExceptionStats();

        public TimeStats getTimeStats() {
            return timeStats;
        }

        public ExceptionStats getExceptionStats() {
            return exceptionStats;
        }
    }

    /**
     * 异常统计
     * 
     * @author yongfeigao
     * @date 2018年9月11日
     */
    public class ExceptionStats {
        private ConcurrentMap<String, AtomicInteger> exceptionMap = new ConcurrentHashMap<String, AtomicInteger>();

        /**
         * 记录异常
         * 
         * @param exception
         */
        public void record(Exception exception) {
            if (exception == null) {
                return;
            }
            String className = exception.getClass().getSimpleName();
            AtomicInteger counter = exceptionMap.get(className);
            if (counter == null) {
                counter = new AtomicInteger();
                AtomicInteger prev = exceptionMap.putIfAbsent(className, counter);
                if (prev != null) {
                    counter = prev;
                }
            }
            counter.incrementAndGet();
        }

        /**
         * 重置
         */
        public void reset() {
            exceptionMap.clear();
        }

        public ConcurrentMap<String, AtomicInteger> getExceptionMap() {
            return exceptionMap;
        }
        
        public Map<String, Object> getMap(){
            Map<String, Object> map = new HashMap<String, Object>();
            for(Entry<String, AtomicInteger> entry : getExceptionMap().entrySet()) {
                map.put(entry.getKey(), entry.getValue().get());
            }
            return map;
        }
    }

    /**
     * 耗时统计
     * 
     * @author yongfeigao
     * @date 2018年9月11日
     */
    public class TimeStats {
        // 最大耗时
        private AtomicReference<Long> maxTimeReference = new AtomicReference<Long>(0L);
        // 调用次数统计
        private AtomicLong count = new AtomicLong();
        // 调用时间统计
        private AtomicLong time = new AtomicLong();

        /**
         * 记录耗时
         * 
         * @param timeInMillis
         */
        public void increment(long timeInMillis) {
            // 记录调用次数
            count.incrementAndGet();
            // 0不用记录
            if(timeInMillis <= 0) {
                return;
            }
            // 记录耗时
            time.addAndGet(timeInMillis);
            // 记录最大耗时
            while (true) {
                long maxTime = maxTimeReference.get();
                if (maxTime >= timeInMillis) {
                    return;
                }
                if (maxTimeReference.compareAndSet(maxTime, timeInMillis)) {
                    break;
                }
            }
        }

        public AtomicReference<Long> getMaxTimeReference() {
            return maxTimeReference;
        }

        public AtomicLong getCount() {
            return count;
        }

        public AtomicLong getTime() {
            return time;
        }
        
        public Map<String, Object> getMap(){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("time", getTime().get());
            map.put("count", getCount().get());
            map.put("maxTime", getMaxTimeReference().get());
            return map;
        }

        /**
         * 重置
         */
        public void reset() {
            maxTimeReference.set(0L);
            count.set(0L);
            time.set(0L);
        }
    }

    /**
     * 调用统计结果
     * 
     * @author yongfeigao
     * @date 2018年9月11日
     */
    public static class InvokeStatsResult {
        // 最大耗时
        private int maxTime;
        // 平均耗时
        private double avgTime;
        // 调用次数
        private int times;
        // 异常集合
        private Map<String, Integer> exceptionMap;

        public void init(TimeStats timeStats) {
            setMaxTime(timeStats.getMaxTimeReference().get().intValue());
            double time = timeStats.getTime().get();
            long count = timeStats.getCount().get();
            // 保留一位小数
            double rst = (long)(time / count * 10) / 10D;
            setAvgTime(rst);
            setTimes((int) count);
        }

        public void init(ExceptionStats exceptionStats) {
            int size = exceptionStats.getExceptionMap().size();
            if (size <= 0) {
                return;
            }
            exceptionMap = new HashMap<String, Integer>();
            for (Entry<String, AtomicInteger> entry : exceptionStats.getExceptionMap().entrySet()) {
                exceptionMap.put(entry.getKey(), entry.getValue().get());
            }
        }

        public int getMaxTime() {
            return maxTime;
        }

        public void setMaxTime(int maxTime) {
            this.maxTime = maxTime;
        }

        public double getAvgTime() {
            return avgTime;
        }

        public void setAvgTime(double avgTime) {
            this.avgTime = avgTime;
        }

        public int getTimes() {
            return times;
        }

        public void setTimes(int times) {
            this.times = times;
        }

        public Map<String, Integer> getExceptionMap() {
            return exceptionMap;
        }

        public void setExceptionMap(Map<String, Integer> exceptionMap) {
            this.exceptionMap = exceptionMap;
        }

        @Override
        public String toString() {
            return "InvokeStatsResult [maxTime=" + maxTime + ", avgTime=" + avgTime + ", times=" + times
                    + ", exceptionMap=" + exceptionMap + "]";
        }
    }
}
