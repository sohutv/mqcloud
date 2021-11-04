package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Range;

/**
 * 用户对象
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年5月28日
 */
public class User {
    // 用户-普通
    public static int ORDINARY = 0;
    // 用户-管理员
    public static int ADMIN = 1;

    // id
    private long id;
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
    // 创建日期
    private Date createDate;
    // 更新日期
    private Date updateTime;
    
    // 接收通知 0：不接收，1：接收
    private int receiveNotice = -1;
    
    // 密码
    private String password;
    
    // 接收手机通知 0：不接收，1：接收
    private int receivePhoneNotice = -1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmailName() {
        return email.substring(0, email.indexOf("@"));
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

    public boolean isAdmin() {
        return ADMIN == type;
    }

    public int getReceiveNotice() {
        return receiveNotice;
    }

    public void setReceiveNotice(int receiveNotice) {
        this.receiveNotice = receiveNotice;
    }

    public int getReceivePhoneNotice() {
        return receivePhoneNotice;
    }

    public void setReceivePhoneNotice(int receivePhoneNotice) {
        this.receivePhoneNotice = receivePhoneNotice;
    }
    
    public boolean receivePhoneNotice() {
        return 1 == receivePhoneNotice;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String notBlankName(){
        if (StringUtils.isBlank(getName())) {
            return getEmailName();
        }
       return getName();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", email=" + email + ", mobile=" + mobile + ", type=" + type
                + ", createDate=" + createDate + ", updateTime=" + updateTime + "]";
    }
}
