package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
/**
 * topic用户参数
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月3日
 */
public class TopicUserParam {
    public static final int PRODUCER  = 1;
    public static final int CONSUMER  = 2;
    // topic name;
    @NotBlank
    private String topic;
    // group name;
    @NotBlank
    private String group;
    // 角色
    @Range(min = 1, max = 2)
    private int role;
    
    private String v;
    
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
    public int getRole() {
        return role;
    }
    public void setRole(int role) {
        this.role = role;
    }
    public boolean isProducer() {
        return PRODUCER == role;
    }
    public boolean isConsumer() {
        return CONSUMER == role;
    }
    public String getV() {
        return v;
    }
    public void setV(String v) {
        this.v = v;
    }
    @Override
    public String toString() {
        return "TopicUserParam [topic=" + topic + ", group=" + group + ", role=" + role + ", v=" + v + "]";
    }
}
