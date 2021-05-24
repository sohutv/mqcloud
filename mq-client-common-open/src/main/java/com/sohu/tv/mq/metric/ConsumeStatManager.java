package com.sohu.tv.mq.metric;

import java.util.HashMap;
import java.util.Map;

/**
 * 消费统计管理
 * 
 * @author yongfeigao
 * @date 2021年4月14日
 */
public class ConsumeStatManager {

    private static ConsumeStatManager instance = new ConsumeStatManager();

    private Map<String, ConsumeThreadStat> consumeThreadMetricsMap = new HashMap<>();

    private Map<String, ConsumeFailedStat> consumeFailedMetricsMap = new HashMap<>();

    private ConsumeStatManager() {
    }

    public static ConsumeStatManager getInstance() {
        return instance;
    }

    /**
     * 注册
     * 
     * @param consuemrGroup
     */
    public void register(String consuemrGroup) {
        consumeThreadMetricsMap.put(consuemrGroup, new ConsumeThreadStat());
        consumeFailedMetricsMap.put(consuemrGroup, new ConsumeFailedStat(10));
    }

    public ConsumeThreadStat getConsumeThreadMetrics(String consuemrGroup) {
        return consumeThreadMetricsMap.get(consuemrGroup);
    }

    public ConsumeFailedStat getConsumeFailedMetrics(String consuemrGroup) {
        return consumeFailedMetricsMap.get(consuemrGroup);
    }
}
