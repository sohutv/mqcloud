package com.sohu.tv.mq.cloud.bo;

import java.util.Date;
/**
 * 客户端版本
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月31日
 */
public class ClientVersion {
    // topic name
    private String topic;
    // producer or consumer
    private String client;
    // 1:producer 2:consumer
    private int role;
    // 版本
    private String version;
    private Date createDate;
    private Date updateTime;
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getClient() {
        return client;
    }
    public void setClient(String client) {
        this.client = client;
    }
    public int getRole() {
        return role;
    }
    public String getRoleDesc() {
        if(1 == role) {
            return "producer";
        }
        return "consumer";
    }
    public void setRole(int role) {
        this.role = role;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public Date getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    public Date getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    @Override
    public String toString() {
        return "ClientVersion [topic=" + topic + ", client=" + client + ", role=" + role + ", version=" + version
                + ", createDate=" + createDate + ", updateTime=" + updateTime + "]";
    }
}
