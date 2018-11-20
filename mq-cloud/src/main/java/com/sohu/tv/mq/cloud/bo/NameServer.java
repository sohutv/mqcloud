package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * NameServer
 * 
 * @author yongfeigao
 * @date 2018年10月23日
 */
public class NameServer {
    // cluster id
    private int cid;
    // ip:port
    private String addr;

    private Date createTime;
    // 检测状态
    private int checkStatus;
    // 检测时间
    private Date checkTime;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getAddr() {
        return addr;
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
}
