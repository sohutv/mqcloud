package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;

import java.util.Arrays;
import java.util.List;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description 取消定时任务参数
 * @date 2023/7/18 09:51:35
 */
public class DelayCancelParam {

    public String topic;

    public Long tid;

    @NotBlank
    public String uniqIds;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getUniqIds() {
        return uniqIds;
    }

    public List<String> getUniqueIdList() {
        return Arrays.asList(uniqIds.split(","));
    }

    public void setUniqIds(String uniqIds) {
        this.uniqIds = uniqIds;
    }
}
