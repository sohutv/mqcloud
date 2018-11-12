package com.sohu.tv.mq.cloud.web.controller.param;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Range;

/**
 * 用户参数
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年11月2日
 */
public class UserParam {
    // 用户名
    private String name;
    // 邮件地址
    @Email
    private String email;
    // 手机
    private String mobile;
    // 用户类型
    @Range(min = 0, max = 1)
    private int type = -1;
    // 密码
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPassword() {
        return DigestUtils.md5Hex(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
