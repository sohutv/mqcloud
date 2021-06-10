package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import com.sohu.tv.mq.cloud.util.DateUtil;

/**
 * broker
 * 
 * @author yongfeigao
 * @date 2018年11月14日
 */
public class Broker {
    
    // cluster id
    private int cid;
    // ip:port
    private String addr;

    // broker id
    private int brokerID;
    // broker name
    private String brokerName;
    
    private Date createTime;
    // 检测状态
    private int checkStatus;
    // 检测时间
    private Date checkTime;

    // 最大偏移量 冗余字段
    private long maxOffset;
    
    // 安装路径
    private String baseDir;
    
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

    public int getBrokerID() {
        return brokerID;
    }

    public void setBrokerID(int brokerID) {
        this.brokerID = brokerID;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
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
    
    public String getIp() {
        return addr.split(":")[0];
    }

    public String getCreateTimeFormat() {
        if(getCreateTime() == null) {
            return null;
        }
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(getCreateTime());
    }
    
    public String getCheckTimeFormat() {
        if(getCheckTime() == null) {
            return "";
        }
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(getCheckTime());
    }
    
    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }
    
    public boolean isMaster() {
        return brokerID == 0;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public String toString() {
        return "Broker [cid=" + cid + ", addr=" + addr + ", brokerID=" + brokerID + ", brokerName=" + brokerName
                + " createTime=" + createTime + ", checkStatus=" + checkStatus + ", checkTime=" + checkTime 
                + ", baseDir=" + baseDir + "]";
    }
}
