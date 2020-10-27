package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import com.sohu.tv.mq.serializable.MessageSerializerEnum;

/**
 * Topic对象
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
public class Topic {
    // topic有序
    public static int HAS_ORDER = 1;
    // topic无序
    public static int NO_ORDER = 0;

    // id
    private long id;
    // cluster id
    private long clusterId;
    // topic name
    private String name;
    // queue num
    private int queueNum;
    // 是否有序 0:无序,1:有序
    private int ordered;
    // 创建日期
    private Date createDate;
    // 更新日期
    private Date updateTime;
    
    // 冗余字段
    private Cluster cluster;
    
    // 消息发送量
    private long count;
    
    // 是否开启trace
    private int traceEnabled;
    
    // topic描述
    private String info;
    
    // 是否延迟消息
    private int delayEnabled;
    
    // 序列化器
    private int serializer;

    // 是否开启流量预警
    private int trafficWarnEnabled;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQueueNum() {
        return queueNum;
    }

    public void setQueueNum(int queueNum) {
        this.queueNum = queueNum;
    }

    public int getOrdered() {
        return ordered;
    }

    public void setOrdered(int ordered) {
        this.ordered = ordered;
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
    
    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public boolean traceEnabled() {
        return traceEnabled == 1;
    }
    
    public int getTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(int traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getDelayEnabled() {
        return delayEnabled;
    }

    public void setDelayEnabled(int delayEnabled) {
        this.delayEnabled = delayEnabled;
    }

    public boolean delayEnabled() {
        return delayEnabled == 1;
    }
    
    public int getSerializer() {
        return serializer;
    }

    public void setSerializer(int serializer) {
        this.serializer = serializer;
    }
    
    public String getSerializerName() {
        return MessageSerializerEnum.getNameByType(serializer);
    }

    public int getTrafficWarnEnabled() {
        return trafficWarnEnabled;
    }

    public void setTrafficWarnEnabled(int trafficWarnEnabled) {
        this.trafficWarnEnabled = trafficWarnEnabled;
    }

    public boolean trafficWarnEnabled() {
        return trafficWarnEnabled == 1;
    }

	@Override
	public String toString() {
		return "Topic [id=" + id + ", clusterId=" + clusterId + ", name=" + name + ", queueNum=" + queueNum
				+ ", ordered=" + ordered + ", createDate=" + createDate + ", updateTime=" + updateTime + ", cluster="
				+ cluster + ", count=" + count + ", traceEnabled=" + traceEnabled + ", info=" + info + ", delayEnabled="
				+ delayEnabled + ", serializer=" + serializer + ", trafficWarnEnabled=" + trafficWarnEnabled + "]";
	}
}
