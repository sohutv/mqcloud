package com.sohu.tv.mq.cloud.web.vo;

import java.util.Map;

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
    // 启动时间
    private String bootTime;
    private Map<String, String> info;
    public String getBootTime() {
        return bootTime;
    }
    public void setBootTime(String bootTime) {
        this.bootTime = bootTime;
    }
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
}
