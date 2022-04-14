package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * 用户警告数
 * 
 * @author yongfeigao
 * @date 2021年9月15日
 */
public class UserWarnCount {
    private Date createDate;
    private int count;
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
