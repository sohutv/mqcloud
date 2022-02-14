package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 用户组参数
 * 
 * @author yongfeigao
 * @date 2021年12月27日
 */
public class UserGroupParam {
    // id
    private long id;
    // 组名
    @NotBlank
    private String name;

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
}
