package com.sohu.tv.mq.header;

import org.apache.rocketmq.remoting.protocol.header.GetTopicStatsInfoRequestHeader;

/**
 * Sohu GetTopicStatsInfoRequestHeader
 *
 * @author yongfeigao
 * @date 2024年09月03日
 */
public class SohuGetTopicStatsInfoRequestHeader extends GetTopicStatsInfoRequestHeader {
    // 是否只获取偏移量
    private boolean onlyOffset;

    public boolean isOnlyOffset() {
        return onlyOffset;
    }

    public void setOnlyOffset(boolean onlyOffset) {
        this.onlyOffset = onlyOffset;
    }
}
