package com.sohu.tv.mq.metric;
/**
 * 消息异常统计
 * 
 * @author yongfeigao
 * @date 2021年4月29日
 */
public class MessageExceptionMetric extends MessageMetric {
    // 异常
    private Throwable exception;
    // 线程id
    private long threadId;
    // 线程名
    private String threadName;

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
}
