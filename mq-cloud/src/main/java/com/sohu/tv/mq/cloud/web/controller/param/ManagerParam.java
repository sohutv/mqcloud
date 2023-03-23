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
}
