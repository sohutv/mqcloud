package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.util.MachineType;

/**
 * 机器类型VO
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月29日
 */
public class MachineTypeVO {

    private int key;
    private String value;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MachineTypeVO(MachineType machineType) {
        setKey(machineType.getKey());
        setValue(machineType.getValue());
    }
}
