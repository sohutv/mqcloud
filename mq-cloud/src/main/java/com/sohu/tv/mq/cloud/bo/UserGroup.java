package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * 用户组
 * 
 * @author yongfeigao
 * @date 2021年12月24日
 */
public class UserGroup {
    // id
    private long id;
    // 组名
    private String name;
    // 创建日期
    private Date createDate;
    // 更新日期
    private Date updateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "UserGroup [id=" + id + ", name=" + name + ", createDate=" + createDate + ", updateTime=" + updateTime
                + "]";
    }
}
