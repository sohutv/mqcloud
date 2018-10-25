package com.sohu.tv.mq.cloud.bo;
/**
 * 通用配置
 * 
 * @author yongfeigao
 * @date 2018年10月16日
 */
public class CommonConfig {
    // id
    private long id;
    
    // 配置key
    private String key;
    
    // 配置值
    private String value;
    
    // 备注
    private String comment;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "CommonConfig [id=" + id + ", key=" + key + ", value=" + value + ", comment=" + comment + "]";
    }
}
