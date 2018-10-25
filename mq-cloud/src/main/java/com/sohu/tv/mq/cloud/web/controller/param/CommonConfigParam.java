package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 通用配置
 * 
 * @author yongfeigao
 * @date 2018年10月16日
 */
public class CommonConfigParam {
    // id
    private long id;
    
    // 配置key
    @NotBlank
    private String key;
    
    // 配置值
    @NotBlank
    private String value;
    
    // 备注
    @NotBlank
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
