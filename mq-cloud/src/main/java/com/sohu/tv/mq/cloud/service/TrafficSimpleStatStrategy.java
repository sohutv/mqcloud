package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.bo.TopicTrafficCheckResult;
import com.sohu.tv.mq.cloud.bo.TopicTrafficStat;
import com.sohu.tv.mq.cloud.bo.TopicTrafficWarnConfig;
import com.sohu.tv.mq.cloud.util.DateUtil;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于简单统计的检测方法
 * @author yongweizhao
 * @create 2020/9/27 16:28
 */
public class TrafficSimpleStatStrategy implements TrafficStatCheckStrategy {

    // 待统计流量
    private List<TopicTraffic> topicTrafficList;
    // 流量统计结果
    private TopicTrafficStat topicTrafficStat;
    // 阈值配置
    private TopicTrafficWarnConfig topicTrafficWarnConfig;

    public TrafficSimpleStatStrategy() {}

    public TrafficSimpleStatStrategy(TopicTrafficStat topicTrafficStat, TopicTrafficWarnConfig topicTrafficWarnConfig) {
        this.topicTrafficStat = topicTrafficStat;
        this.topicTrafficWarnConfig = topicTrafficWarnConfig;
    }

    public TrafficSimpleStatStrategy(List<TopicTraffic> topicTrafficList, TopicTrafficWarnConfig topicTrafficWarnConfig) {
        this.topicTrafficList = topicTrafficList;
        this.topicTrafficWarnConfig = topicTrafficWarnConfig;
    }

    /**
     * 根据一定时间内流量信息统计avgMax、maxMax
     */
    @Override
    public TopicTrafficStat stat() {
        if(CollectionUtils.isEmpty(topicTrafficList)) {
            return null;
        }
        // 以天为维度存储
        Map<Date, List<Long>> countMap = trans2Map(topicTrafficList);
        // 存放每天的最大流量值(去除异常流量点)
        List<Long> maxList = new ArrayList<>();
        for (Map.Entry<Date, List<Long>> entry : countMap.entrySet()) {
            List<Long> values = entry.getValue();
            // 从小到大排序
            Collections.sort(values, (o1, o2) -> (int) (o1 - o2));
            long avg = avg(values);
            long currentMax = 0; // 去除异常值后的最大值
            for (int i = values.size() - 1; i >= 0; i--) {
                long value = values.get(i);
                if (value < (long) (avg * topicTrafficWarnConfig.getAvgMultiplier())) {
                    currentMax = value;
                    break;
                }
            }
            if (currentMax > 0) {
                maxList.add(currentMax);
            }
        }
        if (maxList.size() == 0) {
            return null;
        }
        // 获取maxList的最大值和平均值
        BigDecimal maxSum = new BigDecimal(0);
        long maxMax = 0;
        for (int i = 0; i < maxList.size(); i++) {
            long value = maxList.get(i);
            maxSum = maxSum.add(new BigDecimal(value));
            if (value > maxMax) {
                maxMax = value;
            }
        }
        TopicTrafficStat topicTrafficStat = null;
        long avgMax = maxSum.divide(new BigDecimal(maxList.size()), 2, BigDecimal.ROUND_HALF_UP).longValue();
        if (maxMax > 0 && avgMax > 0) {
            topicTrafficStat = new TopicTrafficStat(topicTrafficList.get(0).getTid(), avgMax, maxMax, countMap.size());

        }
        return topicTrafficStat;
    }

    /**
     * 检测单条数据
     * @param topicTraffic
     * @return TopicTrafficCheckResult
     */
    @Override
    public TopicTrafficCheckResult check(TopicTraffic topicTraffic) {
        if (topicTraffic == null || topicTrafficStat == null ||
                topicTrafficWarnConfig == null) {
            return null;
        }
        TopicTrafficCheckResult checkResult = null;
        // 1. 获取统计值
        long avgMax = topicTrafficStat.getAvgMax();
        long maxMax = topicTrafficStat.getMaxMax();
        // 2. 获取配置
        float avgMaxPercent = topicTrafficWarnConfig.getAvgMaxPercentageIncrease() / 100;
        float maxMaxPercent = topicTrafficWarnConfig.getMaxMaxPercentageIncrease() / 100;
        // 3. 计算流量阈值
        long avgMaxThreshold = (long) (avgMax * (1 + avgMaxPercent));
        long maxMaxThreshold = (long) (maxMax * (1 + maxMaxPercent));
        // 4. 检测
        long count = topicTraffic.getCount();
        if (count > avgMaxThreshold || count > maxMaxThreshold) {
            // 时间格式: YYYY-MM-DD hh:mm
            String warnTime = buildTime(topicTraffic.getCreateDate(), topicTraffic.getCreateTime());
            StringBuilder warnInfo = new StringBuilder();
            warnInfo.append("流量为: ")
                    .append(count)
                    .append("条,相较于前")
                    .append(topicTrafficStat.getDays())
                    .append("天");
            if (count > avgMaxThreshold) {
                int rate = (int)(((count - avgMax) * 100.0 / avgMax) + 0.5);
                warnInfo.append("每天流量最大值的均值: ")
                        .append(avgMax)
                        .append("条,增幅为: ")
                        .append(rate)
                        .append("%");
            } else {
                int rate = (int)(((count - maxMax) * 100.0 / maxMax) + 0.5);
                warnInfo.append("流量的最大值: ")
                        .append(maxMax)
                        .append("条,增幅为: ")
                        .append(rate)
                        .append("%");
            }
            checkResult = new TopicTrafficCheckResult(topicTrafficStat.getTid(), warnTime, warnInfo.toString());
        }
        return checkResult;
    }

    /**
     * 批量检测
     * @param topicTrafficList
     * @return List<TopicTrafficCheckResult>
     */
    @Override
    public List<TopicTrafficCheckResult> check(List<TopicTraffic> topicTrafficList) {
        if (CollectionUtils.isEmpty(topicTrafficList) || topicTrafficStat == null ||
                topicTrafficWarnConfig == null) {
            return null;
        }
        List<TopicTrafficCheckResult> checkResult = new ArrayList<>();
        for (TopicTraffic topicTraffic : topicTrafficList) {
            TopicTrafficCheckResult result = check(topicTraffic);
            if (result != null) {
                checkResult.add(result);
            }
        }
        return checkResult;
    }

    private static Map<Date, List<Long>> trans2Map(List<TopicTraffic> list) {
        Map<Date, List<Long>> countMap = new HashMap<>();
        // 遍历所有数据，按日期划分
        for (int i = 0; i < list.size(); i++) {
            Date date = list.get(i).getCreateDate();
            long count = list.get(i).getCount();
            if (countMap.containsKey(date)) {
                List<Long> list1 = countMap.get(date);
                list1.add(count);
                countMap.put(date, list1);
            } else {
                List<Long> list1 = new ArrayList<>();
                list1.add(count);
                countMap.put(date, list1);
            }
        }
        return countMap;
    }

    private static long avg(List<Long> values) {
        // 总数
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < values.size(); i++) {
            sum = sum.add(new BigDecimal(values.get(i)));
        }
        // 平均数
        long avg = sum.divide(new BigDecimal(values.size()), 2, BigDecimal.ROUND_HALF_UP).longValue();
        return avg;
    }

    private static String buildTime(Date date, String time) {
        String dateStr = DateUtil.getFormat(DateUtil.YMD_DASH).format(date);
        StringBuilder build = new StringBuilder(dateStr);
        build.append(" ");
        build.append(time);
        build.insert(13, ":");
        return build.toString();
    }
}
