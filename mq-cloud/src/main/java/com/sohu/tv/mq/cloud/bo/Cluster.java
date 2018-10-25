package com.sohu.tv.mq.cloud.bo;

/**
 * 集群信息
 * 
 * @author yongfeigao
 * @date 2018年10月10日
 */
public class Cluster {
    
    public static final int ONLINE = 1;
    
    public static final int VIP_CHANNELE_NABLE = 1;
    
    // 集群id
    private int id;
    // 集群名称
    private String name;
    // 是否开启vip通道：1:开启, 0:关闭, rocketmq 4.x版本默认开启
    private int vipChannelEnabled;
    // 是否为线上集群
    private int online;

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
    
    public boolean isEnableVipChannel() {
        return VIP_CHANNELE_NABLE == vipChannelEnabled;
    }

    @Override
    public String toString() {
        return name;
    }
}
