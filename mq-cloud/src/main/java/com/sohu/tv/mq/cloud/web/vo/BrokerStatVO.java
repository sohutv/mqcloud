package com.sohu.tv.mq.cloud.web.vo;

import java.util.Map;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;

/**
 * broker状态
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月9日
 */
public class BrokerStatVO {
    // 地址
    private String brokerAddr;
    // 版本
    private String version;
    // 生成量
    private String inTps;
    // 消息量
    private String outTps;
    // 监控结果 正常-异常
    private int checkStatus;
    // 监控时间
    private String checkTime;
    
    private Map<String, String> info;
    
    public Map<String, String> getInfo() {
        return info;
    }
    public void setInfo(Map<String, String> info) {
        this.info = info;
    }
    public String getBrokerAddr() {
        return brokerAddr;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }
    public String getInTps() {
        return inTps;
    }
    public void setInTps(String inTps) {
        this.inTps = inTps;
    }
    public String getOutTps() {
        return outTps;
    }
    public void setOutTps(String outTps) {
        this.outTps = outTps;
    }
    public int getCheckStatus() {
        return checkStatus;
    }
    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }
    public String getCheckTime() {
        return checkTime;
    }
    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }
    public String getCheckStatusDesc() {
        return CheckStatusEnum.getCheckStatusEnumByStatus(getCheckStatus()).getDesc();
    }
}
