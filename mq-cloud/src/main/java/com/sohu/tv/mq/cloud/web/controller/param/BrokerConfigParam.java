package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * broker配置参数
 * 
 * @author yongfeigao
 * @date 2020年5月19日
 */
public class BrokerConfigParam {
    private int id;
    // group id
    @Range(min = 1)
    private int gid;
    // 属性名
    @NotBlank
    private String key;
    // 属性值
    private String value;
    // 描述
    private String desc;
    // 提示
    private String tip;
    // 是否可以动态修改
    private boolean dynamicModify;
    // 可选值
    private String option;
    // 顺序
    private int order;
    // 是否必选
    private boolean required;

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public boolean isDynamicModify() {
        return dynamicModify;
    }

    public void setDynamicModify(boolean dynamicModify) {
        this.dynamicModify = dynamicModify;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
