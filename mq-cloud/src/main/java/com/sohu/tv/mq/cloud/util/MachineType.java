package com.sohu.tv.mq.cloud.util;

/**
 * 机器类型
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月29日
 */
public enum MachineType {

    UNKNOWN(0, "未知"), 
    PHYSICS(1, "物理机"), 
    VIRTUAL(2, "虚拟机"), 
    DOCKER(3, "docker"),
    ;

    private int key;
    private String value;

    private MachineType(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
