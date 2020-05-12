package com.sohu.tv.mq.cloud.util;
/**
 * 消息延时定义
 * 
 * @author yongfeigao
 * @date 2020年4月26日
 */
public enum MessageDelayLevel {
    LEVEL_1_SECOND(1, "1秒"), 
    LEVEL_5_SECONDS(2, "5秒"), 
    LEVEL_10_SECONDS(3, "10秒"), 
    LEVEL_30_SECONDS(4, "30秒"), 
    LEVEL_1_MINUTE(5, "1分钟"), 
    LEVEL_2_MINUTES(6, "2分钟"), 
    LEVEL_3_MINUTES(7, "3分钟"), 
    LEVEL_4_MINUTES(8, "4分钟"), 
    LEVEL_5_MINUTES(9, "5分钟"), 
    LEVEL_6_MINUTES(10, "6分钟"), 
    LEVEL_7_MINUTES(11, "7分钟"), 
    LEVEL_8_MINUTES(12, "8分钟"), 
    LEVEL_9_MINUTES(13, "9分钟"), 
    LEVEL_10_MINUTES(14, "10分钟"), 
    LEVEL_20_MINUTES(15, "20分钟"), 
    LEVEL_30_MINUTES(16, "30分钟"), 
    LEVEL_1_HOUR(17, "1小时"), 
    LEVEL_2_HOURS(18, "2小时"),
    ;

    private int level;
    private String time;

    private MessageDelayLevel(int level, String time) {
        this.level = level;
        this.time = time;
    }

    public static MessageDelayLevel findByLevel(int level) {
        for (MessageDelayLevel lev : values()) {
            if (level == lev.getLevel()) {
                return lev;
            }
        }
        return null;
    }

    public String getTime() {
        return time;
    }

    public int getLevel() {
        return level;
    }
}
