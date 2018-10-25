package com.sohu.tv.mq.cloud.web.vo;

import org.apache.rocketmq.common.constant.PermName;

/**
 * topic路由
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月10日
 */
public class TopicRoute {
    private String brokerName;
    private int readQueueNums;
    private int writeQueueNums;
    private int perm;
    public String getBrokerName() {
        return brokerName;
    }
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }
    public int getReadQueueNums() {
        return readQueueNums;
    }
    public void setReadQueueNums(int readQueueNums) {
        this.readQueueNums = readQueueNums;
    }
    public int getWriteQueueNums() {
        return writeQueueNums;
    }
    public void setWriteQueueNums(int writeQueueNums) {
        this.writeQueueNums = writeQueueNums;
    }
    public int getPerm() {
        return perm;
    }
    public void setPerm(int perm) {
        this.perm = perm;
    }
    
    public boolean isReadable() {
        return PermName.isReadable(perm);
    }
    
    public boolean isWriteable() {
        return PermName.isWriteable(perm);
    }
}
