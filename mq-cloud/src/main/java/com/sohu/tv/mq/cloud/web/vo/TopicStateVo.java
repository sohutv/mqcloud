package com.sohu.tv.mq.cloud.web.vo;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: 主题状态
 * @date 2022/2/21 14:12
 */
public class TopicStateVo {

    private String createTime;

    // 关联消费者
    private int relationConsumers;

    // 最近三十天生产量
    private long recentMonProMsgNum;

    // 最近三十天消费量
    private long recentMonConMsgNum;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getRelationConsumers() {
        return relationConsumers;
    }

    public void setRelationConsumers(int relationConsumers) {
        this.relationConsumers = relationConsumers;
    }

    public long getRecentMonProMsgNum() {
        return recentMonProMsgNum;
    }

    public void setRecentMonProMsgNum(long recentMonProMsgNum) {
        this.recentMonProMsgNum = recentMonProMsgNum;
    }

    public long getRecentMonConMsgNum() {
        return recentMonConMsgNum;
    }

    public void setRecentMonConMsgNum(long recentMonConMsgNum) {
        this.recentMonConMsgNum = recentMonConMsgNum;
    }
}
