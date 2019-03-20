package com.sohu.tv.mq.cloud.bo;

/**
 * 集群信息
 * 
 * @author yongfeigao
 * @date 2018年10月10日
 */
public class Cluster {
    
    public static final int ONLINE = 1;
    // 表示肯定
    public static final int YES = 1;
    
    // 集群id
    private int id;
    // 集群名称
    private String name;
    // 是否开启vip通道：1:开启, 0:关闭, rocketmq 4.x版本默认开启
    private int vipChannelEnabled;
    // 是否为线上集群
    private int online;
    
    private int transactionEnabled;
    
    private int traceEnabled;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVipChannelEnabled() {
        return vipChannelEnabled;
    }

    public void setVipChannelEnabled(int vipChannelEnabled) {
        this.vipChannelEnabled = vipChannelEnabled;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }
    
    public boolean online() {
        return ONLINE == online;
    }
    
    public int getTransactionEnabled() {
        return transactionEnabled;
    }

    public void setTransactionEnabled(int transactionEnabled) {
        this.transactionEnabled = transactionEnabled;
    }

    public int getTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(int traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public boolean isEnableVipChannel() {
        return YES == vipChannelEnabled;
    }
    
    public boolean isEnableTrace() {
        return YES == traceEnabled;
    }
    
    public boolean isEnableTransaction() {
        return YES == transactionEnabled;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Cluster other = (Cluster) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
