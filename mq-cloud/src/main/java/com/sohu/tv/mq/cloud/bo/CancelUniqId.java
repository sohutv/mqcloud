package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description
 * @date 2023/7/28 16:41:39
 */
public class CancelUniqId {

    /**
     * Topic id
     */
    private Long tid;

    /**
     * 已取消消息msgId
     */
    private String uniqueId;

    /**
     * 创建时间
     */
    private Date createTime;

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
