package com.sohu.tv.mq.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
/**
 * 时间段统计，用于统计采样百分位数
 * 
 * 采用非准确耗时统计，即将耗时进行分段，再统计耗时次数。
 * 共三段，其中0~10为一段，11~100为一段，100以上为一段，如下：
 * [0~10]: 为精确统计
 * [11~15]: 统一为15ms
 * [16~20]: 统一为20ms
 * 。。。
 * [96~100]: 统一为100ms
 * [101~150]: 统一为150ms
 * [151~200]: 统一为200ms
 * 。。。
 * 之后类似
 * @author yongfeigao
 * @date 2018年9月10日
 */
public class TimeSectionStats {

    // 计数器
    private AtomicLong[] timeSectionCounter;
    
    // 时间段
    private List<Integer> timeSection;
    
    // 最大索引
    private int maxIndex;
    
    // 时间段统计采样器
    private TimeSectionStatsSampler timeSectionStatsSampler;

    /**
     * 构造方法
     * @param timeInMillis 期望的最大响应时间
     */
    public TimeSectionStats(int timeInMillis) {
        timeSectionCounter = timeSection(timeInMillis);
        timeSectionStatsSampler = new TimeSectionStatsSampler();
    }

    /**
     * 耗时增加，线程安全
     * @param timeInMillis
     */
    public void increment(int timeInMillis) {
        int index = index(timeInMillis);
        // 增加兼容性逻辑
        if(index < 0) {
            index = 0;
        }
        if(index >= timeSectionCounter.length) {
            index = timeSectionCounter.length - 1;
        }
        long rst = timeSectionCounter[index].incrementAndGet();
        // 溢出后重新计数
        if(rst < 0) {
            timeSectionCounter[index].set(1);
        }
    }
    
    /**
     * 根据时间拆分为时间间隔
     * 
     * @param timeInMillis
     * @return
     */
    private AtomicLong[] timeSection(int timeInMillis) {
        // 获取切分后的时间段
        initTimeSection(timeInMillis);
        // 构造时间段
        AtomicLong[] timeSectionCounter = new AtomicLong[timeSection.size()];
        for (int i = 0; i < timeSectionCounter.length; ++i) {
            timeSectionCounter[i] = new AtomicLong();
        }
        maxIndex = timeSectionCounter.length - 1;
        return timeSectionCounter;
    }
    
    /**
     * 根据索引获取时间
     * @param index
     * @return
     */
    public int time(int index) {
        if(index > maxIndex) {
            index = maxIndex;
        }
        return timeSection.get(index);
    }

    /**
     * 根据timeInMillis计算其所在的加标
     * 例如12，对应的区域为[10,15], 下标为11
     * 例如16，对应的区域为[16,20], 下标为12
     * @param timeInMillis
     * @return
     */
    public int index(int timeInMillis) {
        if (timeInMillis < 10) {
            return timeInMillis;
        }
        if (timeInMillis < 100) {
            return _index(timeInMillis, 10, 10);
        }
        return _index(timeInMillis, 100, 28);
    }
    
    /**
     * 下标计算
     * @param time
     * @param section
     * @param prevIndex
     * @return
     */
    private int _index(int time, int section, int prevIndex) {
        int base = time / section;
        base = (base - 1) * 2;
        int left = time % section;
        if(left > section / 2) {
            base += 2;
        } else if(left > 0) {
            base += 1;
        }
        return prevIndex + base;
    }

    /**
     * 将timeInMillis按照如下规则拆分成时间间隔: 
     * 1. 0~10与下标对应关系不变：
     * 例如 [0~10]<->[0~10]
     * 2. 大于10小于100：按照15,20,25 ...，即每次按照5毫秒递增 
     * 例如 (10~15]<->11
     * 3. 大于100：按照50毫秒递增
     * 例如 (100~150]<->11
     * @param timeInMillis
     * @return 切分后的时间段数量
     */
    private void initTimeSection(int timeInMillis) {
        this.timeSection = new ArrayList<Integer>();
        int costTime = 0;
        while (costTime < timeInMillis) {
            timeSection.add(costTime);
            if (costTime < 10) {
                ++costTime;
            } else if (costTime < 100) {
                costTime += 5;
            } else {
                costTime += 50;
            }
        }
        timeSection.add(timeInMillis);
    }

    public List<Integer> getTimeSection() {
        return timeSection;
    }

    public AtomicLong[] getTimeSectionCounter() {
        return timeSectionCounter;
    }
    
    /**
     * 采样
     */
    public void sample() {
        timeSectionStatsSampler.sample();
    }
    
    /**
     * 对百分位数进行统计
     * 
     * @param percentile
     * @return
     */
    public int percentile(double percentile) {
        return timeSectionStatsSampler.percentile(percentile);
    }

    /**
     * 计算总时间
     * @return
     */
    public long totalTime() {
        return timeSectionStatsSampler.totalTime();
    }
    
    public long[] getSnapshotData() {
        return timeSectionStatsSampler.snapshotData;
    }

    public long[] getSampledData() {
        return timeSectionStatsSampler.sampledData;
    }
    
    public long getTotalCount() {
        return timeSectionStatsSampler.totalCount;
    }
    
    
    /**
     * 时间间隔采样器
     * 
     * @author yongfeigao
     * @date 2018年9月10日
     */
    public class TimeSectionStatsSampler {
        // 快照数据
        private long[] snapshotData;
        // 计算后的采样数据
        private long[] sampledData;
        // 总请求次数
        private long totalCount;

        /**
         * 数据采样
         */
        public void sample() {
            // 获取计数
            AtomicLong[] timeSectionCounterArray = getTimeSectionCounter();
            if (snapshotData == null) {
                snapshotData = new long[timeSectionCounterArray.length];
            }
            if (sampledData == null) {
                sampledData = new long[timeSectionCounterArray.length];
            }
            totalCount = 0;
            for (int i = 0; i < timeSectionCounterArray.length; ++i) {
                // 获取当前计数
                long count = timeSectionCounterArray[i].get();
                // 计算统计周期内的次数
                long times = count - snapshotData[i];
                // 数据溢出处理
                if(times < 0) {
                    sampledData[i] = count;
                } else {
                    sampledData[i] = times;
                }
                // 保存本来统计总次数
                totalCount += sampledData[i];
                // 保存当前次数
                snapshotData[i] = count;
            }
        }

        /**
         * 对百分位数进行统计
         * 
         * @param percentile
         * @return
         */
        public int percentile(double percentile) {
            if (totalCount == 0) {
                return -1;
            }
            long curCount = 0;
            for (int i = 0; i < sampledData.length; ++i) {
                if (sampledData[i] == 0) {
                    continue;
                }
                curCount += sampledData[i];
                double curPercentile = (double) curCount / totalCount;
                if (percentile - curPercentile <= 0.0001) {
                    return time(i);
                }
            }
            return -1;
        }

        /**
         * 计算总耗时
         * 
         * @return
         */
        public long totalTime() {
            if (totalCount == 0) {
                return 0;
            }
            long totalTime = 0;
            for (int i = 0; i < sampledData.length; ++i) {
                if (sampledData[i] == 0) {
                    continue;
                }
                totalTime += sampledData[i] * time(i);
            }
            return totalTime;
        }
    }
}
