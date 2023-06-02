package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * Proxy
 * 
 * @author yongfeigao
 * @date 2023年05月25日
 */
public class Proxy {
    // cluster id
    private int cid;
    // ip:port
    private String addr;

    private Date createTime;
    // 检测状态
    private int checkStatus;
    // 检测时间
    private Date checkTime;
    // 安装路径
    private String baseDir;
    // 配置项
    private String config;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getAddr() {
        return addr;
    }
    
    public String getIp() {
        return addr.split(":")[0];
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public String getCheckStatusDesc() {
        return CheckStatusEnum.getCheckStatusEnumByStatus(getCheckStatus()).getDesc();
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "cid=" + cid +
                ", addr='" + addr + '\'' +
                ", createTime=" + createTime +
                ", checkStatus=" + checkStatus +
                ", checkTime=" + checkTime +
                ", baseDir='" + baseDir + '\'' +
                ", config='" + config + '\'' +
                '}';
    }
}
