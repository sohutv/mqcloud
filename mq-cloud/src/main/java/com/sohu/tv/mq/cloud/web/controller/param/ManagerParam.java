package com.sohu.tv.mq.cloud.web.controller.param;

import org.apache.commons.lang3.StringUtils;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: 主题过滤条件
 * @date 2022/2/13 15:28
 */
public class ManagerParam {

    // 集群ID
    private Long cid;

    // 组织ID
    private Long gid;

    // 用户ID
    private Long uid;

    // 客户端语言
    private String language;

    // 客户端名称
    private String groupName;

    // 当日生产为0
    private Boolean nonePrducerFlows;

    // 无消费者
    private Boolean noneConsumers;

    // 无消费量
    private Boolean noneConsumerFlows;

    private String topic;

    private Integer orderType;

    //请求参数
    private String queryStr;


    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getGid() {
        return gid;
    }

    public void setGid(Long gid) {
        this.gid = gid;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getNonePrducerFlows() {
        return nonePrducerFlows;
    }

    public void setNonePrducerFlows(Boolean nonePrducerFlows) {
        this.nonePrducerFlows = nonePrducerFlows;
    }

    public Boolean getNoneConsumers() {
        return noneConsumers;
    }

    public void setNoneConsumers(Boolean noneConsumers) {
        this.noneConsumers = noneConsumers;
    }

    public Boolean getNoneConsumerFlows() {
        return noneConsumerFlows;
    }

    public void setNoneConsumerFlows(Boolean noneConsumerFlows) {
        this.noneConsumerFlows = noneConsumerFlows;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public void buildQueryStr(){
        StringBuilder builder = new StringBuilder();
        if (cid !=null){
            builder.append("cid=").append(cid);
        }
        if (gid!= null){
            builder.append("&gid=").append(gid);
        }
        if (uid !=null){
            builder.append("&uid=").append(uid);
        }
        if (noneConsumers != null){
            builder.append("&noneConsumers=").append(noneConsumers);
        }
        if (nonePrducerFlows != null){
            builder.append("&nonePrducerFlows=").append(nonePrducerFlows);
        }
        if (noneConsumerFlows != null){
            builder.append("&noneConsumerFlows=").append(noneConsumerFlows);
        }
        if (StringUtils.isNotBlank(language)){
            builder.append("&language=").append(language);
        }
        if (StringUtils.isNotBlank(groupName)){
            builder.append("&groupName=").append(groupName);
        }
        if (StringUtils.isNotBlank(topic)) {
            builder.append("&topic=").append(topic);
        }
        if (orderType != null){
            builder.append("&orderType=").append(orderType);
        }
        String query = builder.toString();
        if (query.startsWith("&")){
            query = query.substring(1);
        }
        if (StringUtils.isNoneBlank(query)){
            this.queryStr = query;
        }
    }

    public String getQueryStr() {
        return queryStr;
    }

    public void setQueryStr(String queryStr) {
        this.queryStr = queryStr;
    }

    @Override
    public String toString() {
        return "ManagerParam{" +
                "cid=" + cid +
                ", gid=" + gid +
                ", uid=" + uid +
                ", language=" + language +
                ", nonePrducerFlows=" + nonePrducerFlows +
                ", noneConsumers=" + noneConsumers +
                ", noneConsumerFlows=" + noneConsumerFlows +
                '}';
    }

    public static enum QueryOrderType{
        TRAFFIC_DESC(1, "traffic"),
        TOPICNAME_ASC(2, "topicName"),
        CREATE_ASC(3, "createTime"),
        SIZE1D_DESC(4, "size1d"),
        SIZE2D_DESC(5, "size2d"),
        SIZE3D_DESC(6, "size3d"),
        SIZE5D_DESC(7, "size5d"),
        SIZE7D_DESC(8, "size7d"),
        SIZE_DESC(9, "size"),
        ;

        private final int type;
        private final String name;

        private QueryOrderType(int type, String name) {
            this.type = type;
            this.name = name;
        }

        public static QueryOrderType getQueryOrderTypeByDefault(Integer type, QueryOrderType defaultQueryOrderType) {
            if (type == null) {
                return defaultQueryOrderType;
            }
            for (QueryOrderType queryOrderType : QueryOrderType.values()) {
                if (queryOrderType.getType() == type) {
                    return queryOrderType;
                }
            }
            return defaultQueryOrderType;
        }

        public int getType() {
            return type;
        }
    }
}
