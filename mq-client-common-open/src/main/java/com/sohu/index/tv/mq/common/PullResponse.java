package com.sohu.index.tv.mq.common;

import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * 拉取的响应
 *
 * @author: yongfeigao
 * @date: 2022/6/17 16:50
 */
public class PullResponse {
    private Status status;
    private long nextOffset;
    private long minOffset;
    private long maxOffset;
    private List<MessageExt> msgList;

    public static PullResponse build(Status status) {
        PullResponse pullResponse = new PullResponse();
        pullResponse.setStatus(status);
        return pullResponse;
    }

    public static PullResponse build(PullResult pullResult) {
        PullResponse pullResponse = new PullResponse();
        pullResponse.setMinOffset(pullResult.getMinOffset());
        pullResponse.setMaxOffset(pullResult.getMaxOffset());
        pullResponse.setNextOffset(pullResult.getNextBeginOffset());
        pullResponse.setMsgList(pullResult.getMsgFoundList());
        pullResponse.setStatus(Status.find(pullResult.getPullStatus()));
        return pullResponse;
    }

    public boolean isFoundStatus() {
        return Status.FOUND == status;
    }

    public boolean isOffsetErrorStatus() {
        return Status.OFFSET_ILLEGAL == status || Status.NO_MATCHED_MSG == status;
    }

    public boolean isNoNewMsgStatus() {
        return Status.NO_NEW_MSG == status;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(long nextOffset) {
        this.nextOffset = nextOffset;
    }

    public long getMinOffset() {
        return minOffset;
    }

    public void setMinOffset(long minOffset) {
        this.minOffset = minOffset;
    }

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public List<MessageExt> getMsgList() {
        return msgList;
    }

    public void setMsgList(List<MessageExt> msgList) {
        this.msgList = msgList;
    }

    public static enum Status {
        /**
         * Founded
         */
        FOUND,
        /**
         * No new message can be pull
         */
        NO_NEW_MSG,
        /**
         * Filtering results can not match
         */
        NO_MATCHED_MSG,
        /**
         * Illegal offset,may be too big or too small
         */
        OFFSET_ILLEGAL,
        /**
         * pause
         */
        PAUSED,
        /**
         * 限速
         */
        RATE_LIMITED,
        /**
         * 未知
         */
        UNKNOWN,
        ;

        public static Status find(PullStatus pullStatus) {
            switch (pullStatus) {
                case FOUND:
                    return Status.FOUND;
                case NO_NEW_MSG:
                    return Status.NO_NEW_MSG;
                case NO_MATCHED_MSG:
                    return Status.NO_MATCHED_MSG;
                case OFFSET_ILLEGAL:
                    return Status.OFFSET_ILLEGAL;
                default:
                    return Status.UNKNOWN;
            }
        }
    }
}
