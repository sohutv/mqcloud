package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import com.sohu.tv.mq.cloud.common.util.WebUtil;
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

    // 冗余字段 生产者名称
    private String producerName;

    // 冗余字段 消费者名称
    private String consumerName;

    // 近1小时消息发送量
    private long count;

    // 近1小时消息大小
    private long size;
    
    // 是否开启trace
    private int traceEnabled;
    
    // topic描述
    private String info;
    
    // 是否延迟消息
    private int delayEnabled;
    
    // 序列化器
    private int serializer;

    // 是否开启流量突增预警
    private int trafficWarnEnabled;

    // 状态确认 0 未确认 1 确认
    private int effective;

    // 1天前的流量
    private long size1d;
    // 2天前的流量
    private long size2d;
    // 3天前的流量
    private long size3d;
    // 5天前的流量
    private long size5d;
    // 7天前的流量
    private long size7d;

    // 1天前的消息量
    private long count1d;
    // 2天前的消息量
    private long count2d;

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

    public boolean isOrderedTopic() {
        return ordered == HAS_ORDER;
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

    public int getEffective() {
        return effective;
    }

    public void setEffective(int effective) {
        this.effective = effective;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize1d() {
        return size1d;
    }

    public void setSize1d(long size1d) {
        this.size1d = size1d;
    }

    public long getSize2d() {
        return size2d;
    }

    public void setSize2d(long size2d) {
        this.size2d = size2d;
    }

    public long getSize3d() {
        return size3d;
    }

    public void setSize3d(long size3d) {
        this.size3d = size3d;
    }

    public long getSize5d() {
        return size5d;
    }

    public void setSize5d(long size5d) {
        this.size5d = size5d;
    }

    public long getSize7d() {
        return size7d;
    }

    public void setSize7d(long size7d) {
        this.size7d = size7d;
    }

    public String getSize1dFormat() {
        return WebUtil.sizeFormat(size1d);
    }

    public String getSize2dFormat() {
        return WebUtil.sizeFormat(size2d);
    }

    public String getSize3dFormat() {
        return WebUtil.sizeFormat(size3d);
    }

    public String getSize5dFormat() {
        return WebUtil.sizeFormat(size5d);
    }

    public String getSize7dFormat() {
        return WebUtil.sizeFormat(size7d);
    }

    public String getCountFormat() {
        return WebUtil.countFormat(count);
    }

    public String getSizeFormat() {
        return WebUtil.sizeFormat(size);
    }

    public long getCount1d() {
        return count1d;
    }

    public void setCount1d(long count1d) {
        this.count1d = count1d;
    }

    public long getCount2d() {
        return count2d;
    }

    public void setCount2d(long count2d) {
        this.count2d = count2d;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", clusterId=" + clusterId +
                ", name='" + name + '\'' +
                ", queueNum=" + queueNum +
                ", ordered=" + ordered +
                ", createDate=" + createDate +
                ", updateTime=" + updateTime +
                ", cluster=" + cluster +
                ", producerName='" + producerName + '\'' +
                ", consumerName='" + consumerName + '\'' +
                ", count=" + count +
                ", size=" + size +
                ", traceEnabled=" + traceEnabled +
                ", info='" + info + '\'' +
                ", delayEnabled=" + delayEnabled +
                ", serializer=" + serializer +
                ", trafficWarnEnabled=" + trafficWarnEnabled +
                ", effective=" + effective +
                ", size1d=" + size1d +
                ", size2d=" + size2d +
                ", size3d=" + size3d +
                ", size5d=" + size5d +
                ", size7d=" + size7d +
                '}';
    }
}
