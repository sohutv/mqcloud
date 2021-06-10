package com.sohu.tv.mq.cloud.bo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.sohu.tv.mq.cloud.util.Jointer;
/**
 * 客户端版本
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月31日
 */
public class ClientVersion {
    public static final int PRODUCER = 1;
    public static final int CONSUMER = 2;
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
    // 客户端归属的用户
    private Set<User> owners;
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
    public String getOwnersString() {
        return Jointer.BY_COMMA.join(owners, v -> v.notBlankName());
    }
    public boolean addOwner(User owner) {
        if(owners == null) {
            owners = new HashSet<>();
        }
        return owners.add(owner);
    }
    public Set<User> getOwners() {
        return owners;
    }
    public void setOwners(Set<User> owners) {
        this.owners = owners;
    }
    
    @Override
    public String toString() {
        return "ClientVersion [topic=" + topic + ", client=" + client + ", role=" + role + ", version=" + version
                + ", createDate=" + createDate + ", updateTime=" + updateTime + "]";
    }
}
