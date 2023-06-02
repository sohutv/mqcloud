package com.sohu.tv.mq.cloud.util;

/**
 * rocketmq版本
 *
 * @Auther: yongfeigao
 * @Date: 2023/5/19
 */
public enum RocketMQVersion {
    V4("4"),
    V5("5"),
    ;
    private String version;

    RocketMQVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public static RocketMQVersion getRocketMQVersion(String version) {
        for (RocketMQVersion rocketMQVersion : values()) {
            if (rocketMQVersion.getVersion().equals(version)) {
                return rocketMQVersion;
            }
        }
        return null;
    }
}
