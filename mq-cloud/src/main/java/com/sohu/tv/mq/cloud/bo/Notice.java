package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import com.sohu.tv.mq.cloud.util.DateUtil;
/**
 * 通知
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
public class Notice {
    private long id;
    private String content;
    private int status;
    private Date createDate;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public Date getCreateDate() {
        return createDate;
    }
    public String getCreateDateFormat() {
        if(getCreateDate() == null) {
            return null;
        }
        return DateUtil.getFormat(DateUtil.YMD_DASH).format(getCreateDate());
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
