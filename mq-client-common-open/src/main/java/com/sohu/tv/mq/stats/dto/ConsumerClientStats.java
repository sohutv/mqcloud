package com.sohu.tv.mq.stats.dto;

import com.sohu.tv.mq.stats.InvokeStats.InvokeStatsResult;

/**
 * 客户端消费者统计
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/27
 */
public class ConsumerClientStats {
    // 客户端Id
    private String clientId;
    // consumer
    private String consumer;
    // 统计时间 需要*60000，才是毫秒
    private int statTime;
    // 统计数据
    private InvokeStatsResult stats;

    public ConsumerClientStats() {
    }

    public ConsumerClientStats(String consumer, String clientId, InvokeStatsResult statsResult) {
        this.consumer = consumer;
        this.clientId = clientId;
        this.stats = statsResult;
        this.statTime = (int) (System.currentTimeMillis() / 60000);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public int getStatTime() {
        return statTime;
    }

    public void setStatTime(int statTime) {
        this.statTime = statTime;
    }

    public InvokeStatsResult getStats() {
        return stats;
    }

    public void setStats(InvokeStatsResult stats) {
        this.stats = stats;
    }
}
