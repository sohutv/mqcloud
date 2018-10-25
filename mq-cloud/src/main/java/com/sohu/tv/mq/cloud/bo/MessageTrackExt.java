package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.apache.rocketmq.tools.admin.api.TrackType;

public class MessageTrackExt extends MessageTrack {

    public String getTrackTypeDesc() {
        TrackType trackType = getTrackType();
        switch (trackType) {
            case CONSUMED:
                return "消费成功";
            case CONSUMED_BUT_FILTERED:
                return "消费成功(被过滤)";
            case NOT_CONSUME_YET:
                return "未消费";
            case NOT_ONLINE:
                return "不在线";
            case PULL:
                return "主动拉取";
            case UNKNOWN:
                return "未知";
        }
        return "异常";
    }

}
