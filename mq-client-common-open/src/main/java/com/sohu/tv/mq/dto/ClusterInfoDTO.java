package com.sohu.tv.mq.dto;

import com.sohu.tv.mq.util.MQProtocol;

/**
 * 集群信息
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月3日
 */
public class ClusterInfoDTO {
    // 集群id
    private int clusterId;
    // 是否启用vip通道
    private boolean vipChannelEnabled;
    // 是否广播消费
    private boolean broadcast;
    // 是否开启trace
    private boolean traceEnabled;
    // 序列化器
    private int serializer;
    // 通信协议
    private int protocol;
    // acl accessKey
    private String accessKey;
    // acl secretKey
    private String secretKey;
    
    public int getClusterId() {
        return clusterId;
    }
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }
    public boolean isVipChannelEnabled() {
        return vipChannelEnabled;
    }
    public void setVipChannelEnabled(boolean vipChannelEnabled) {
        this.vipChannelEnabled = vipChannelEnabled;
    }
    public boolean isBroadcast() {
        return broadcast;
    }
    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }
    public boolean isTraceEnabled() {
        return traceEnabled;
    }
    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }
    public int getSerializer() {
        return serializer;
    }
    public void setSerializer(int serializer) {
        this.serializer = serializer;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public boolean isProxyRemoting() {
        return MQProtocol.isProxyRemoting(protocol);
    }

    @Override
    public String toString() {
        return "ClusterInfoDTO [clusterId=" + clusterId + ", vipChannelEnabled=" + vipChannelEnabled + ", broadcast="
                + broadcast + ", traceEnabled=" + traceEnabled + ", serializer=" + serializer + "]";
    }
}
