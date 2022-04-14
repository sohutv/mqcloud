package com.sohu.tv.mq.cloud.web.vo;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: TODO
 * @date 2022/3/1 9:43
 */
public class ConsumerStateVo {

    // 是否是唯一关联，该主题是否只存在该消费者 0 否  1 是
    private int onlyRelation;

    // 最近三十天消费量
    private long recentMonConMsgNum;

    public int getOnlyRelation() {
        return onlyRelation;
    }

    public void setOnlyRelation(int onlyRelation) {
        this.onlyRelation = onlyRelation;
    }

    public long getRecentMonConMsgNum() {
        return recentMonConMsgNum;
    }

    public void setRecentMonConMsgNum(long recentMonConMsgNum) {
        this.recentMonConMsgNum = recentMonConMsgNum;
    }
}
