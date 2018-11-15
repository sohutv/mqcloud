package com.sohu.tv.mq.cloud.bo;
/**
 * 检测状态
 * 
 * @author yongfeigao
 * @date 2018年11月14日
 */
public enum CheckStatusEnum {
    UNKONWN(0, "未知"),
    OK(1, "正常"),
    FAIL(2, "异常"),
    ;
    private int status;
    private String desc;
    
    private CheckStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }
    
    public int getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static CheckStatusEnum getCheckStatusEnumByStatus(int status) {
        for(CheckStatusEnum checkStatusEnum : values()) {
            if(checkStatusEnum.getStatus() == status) {
                return checkStatusEnum;
            }
        }
        return UNKONWN;
    }
}
