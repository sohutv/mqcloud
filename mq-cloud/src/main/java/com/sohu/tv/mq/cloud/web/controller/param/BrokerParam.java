package com.sohu.tv.mq.cloud.web.controller.param;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.service.MQDeployer;

/**
 * broker参数
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月15日
 */
public class BrokerParam {
    public static final String SLAVE = "SLAVE";
    
    private String ip;
    private String brokerName;
    private String dir;
    private String brokerRole;
    private int port;
    private String flushDiskType;
    private int fileReservedTime;
    private int mqClusterId;
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getBrokerRole() {
        return brokerRole;
    }
    public void setBrokerRole(String brokerRole) {
        this.brokerRole = brokerRole;
    }
    public String getFlushDiskType() {
        return flushDiskType;
    }
    public void setFlushDiskType(String flushDiskType) {
        this.flushDiskType = flushDiskType;
    }
    public int getFileReservedTime() {
        return fileReservedTime;
    }
    public void setFileReservedTime(int fileReservedTime) {
        this.fileReservedTime = fileReservedTime;
    }
    public int getMqClusterId() {
        return mqClusterId;
    }
    public void setMqClusterId(int mqClusterId) {
        this.mqClusterId = mqClusterId;
    }
    public String getDir() {
        return dir;
    }
    public void setDir(String dir) {
        this.dir = dir;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public boolean isSlave() {
        if(SLAVE.equals(getBrokerRole())) {
            return true;
        }
        return false;
    }
    
    public int getBrokerRoleID() {
        if(SLAVE.equals(getBrokerRole())) {
            return 1;
        }
        return 0;
    }
    
    public String getBrokerName() {
        return brokerName;
    }
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }
    public String toConfig(String nameServerDomain, Cluster cluster) {
        String config = "brokerClusterName=" + cluster.getName()
                + "\nbrokerName=" + getBrokerName()
                + "\nbrokerId=" + ("SLAVE".equals(brokerRole) ? "1" : "0")
                + "\nlistenPort=" + port
                + "\nbrokerIP1=" + ip
                + (isSlave() ? "" : "\nbrokerIP2=" + ip)
                + "\nbrokerRole=" + brokerRole
                + "\nflushDiskType=" + flushDiskType
                + "\ndeleteWhen=04"
                /** fix for TIMEOUT_CLEAN_QUEUE] issue **/
                + "\nsendMessageThreadPoolNums=48"
                + "\nuseReentrantLockWhenPutMessage=true"
                + "\nwaitTimeMillsInSendQueue=500"
                /** fix for TIMEOUT_CLEAN_QUEUE] issue end **/
                + "\nfileReservedTime=" + fileReservedTime
                + "\nrmqAddressServerDomain=" + nameServerDomain
                + "\nrmqAddressServerSubGroup=" + String.format(MQDeployer.NS_SUB_GROUP, cluster.getId())
                + "\nfetchNamesrvAddrByAddressServer=true"
                + "\nautoCreateTopicEnable=false"
                + "\nclusterTopicEnable=false"
                + "\nautoCreateSubscriptionGroup=true"
                + "\nstorePathRootDir=" + MQDeployer.MQ_CLOUD_DIR + getDir() + "/data"
                + "\nstorePathCommitLog=" + MQDeployer.MQ_CLOUD_DIR + getDir() + "/data/commitlog"
                + "\nstorePathIndex=" + MQDeployer.MQ_CLOUD_DIR + getDir() + "/data/index"
                + "\nstoreCheckpoint=" + MQDeployer.MQ_CLOUD_DIR + getDir() + "/data/checkpoin"
                + "\nabortFile=" + MQDeployer.MQ_CLOUD_DIR + getDir() + "/data/abort";
        return config;
    }
    
}
