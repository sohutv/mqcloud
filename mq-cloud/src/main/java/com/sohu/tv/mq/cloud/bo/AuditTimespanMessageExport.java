package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.util.DateUtil;

import java.util.Date;

/**
 * 时间段消息导出
 *
 * @author yongfeigao
 * @date 2023/9/21
 */
public class AuditTimespanMessageExport {
    private long aid;
    // 从哪个topic拉取消息
    private String topic;
    // 开始时间戳
    private long start;
    // 结束时间戳
    private long end;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getStartFormat() {
        return DateUtil.getFormat(DateUtil.YMD_BLANK_HMS_COLON).format(new Date(start));
    }

    public String getEndFormat() {
        return DateUtil.getFormat(DateUtil.YMD_BLANK_HMS_COLON).format(new Date(end));
    }

    @Override
    public String toString() {
        return "AuditTimespanMessageExport{" +
                "aid=" + aid +
                ", topic='" + topic + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
