package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * 时间段消息导出
 *
 * @author yongfeigao
 * @date 2023/9/21
 */
public class TimespanMessageExportParam {
    // 从哪个topic拉取消息
    @NotBlank
    private String topic;
    // 开始时间戳
    @Range(min = 1)
    private long start;
    // 结束时间戳
    @Range(min = 1)
    private long end;

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
}
