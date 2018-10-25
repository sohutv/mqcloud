package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * 通知参数
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
public class NoticeParam {
    private long id;
    @NotBlank
    private String content;
    @Range(min = 0, max = 1)
    private int status;
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
}
