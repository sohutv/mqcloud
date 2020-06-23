package com.sohu.tv.mq.cloud.bo;

/**
 * broker配置组
 * 
 * @author yongfeigao
 * @date 2020年5月18日
 */
public class BrokerConfigGroup {
    // id
    private int id;
    // 组
    private String group;
    // 序号
    private int order;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "BrokerConfigGroup [id=" + id + ", group=" + group + ", order=" + order + "]";
    }
}
