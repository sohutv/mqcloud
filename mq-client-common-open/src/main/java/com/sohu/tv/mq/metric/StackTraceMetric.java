package com.sohu.tv.mq.metric;

import java.lang.Thread.State;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 线程统计
 * 
 * @author yongfeigao
 * @date 2021年4月14日
 */
public class StackTraceMetric {
    // 开始时间
    private long startTime;
    // 消费的消息id
    private List<String> msgIdList;
    // 线程id
    private long id;
    // 线程名
    private String name;
    // 线程状态
    private State state;
    // 线程堆栈
    private StackTraceElement[] stackTraceArray;
    
    // 消息
    private String message;
    
    // 错误类
    private String errorClass;

    public StackTraceMetric() {
    }

    public StackTraceMetric(long startTime, List<String> msgIdList) {
        this.startTime = startTime;
        this.msgIdList = msgIdList;
    }

    public void initThreadMetric(Thread thread) {
        this.id = thread.getId();
        this.name = thread.getName();
        this.state = thread.getState();
        this.stackTraceArray = thread.getStackTrace();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public StackTraceElement[] getStackTraceArray() {
        return stackTraceArray;
    }

    public void setStackTraceArray(StackTraceElement[] stackTraceArray) {
        this.stackTraceArray = stackTraceArray;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getFormattedStartTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime));
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public List<String> getMsgIdList() {
        return msgIdList;
    }

    public void setMsgIdList(List<String> msgIdList) {
        this.msgIdList = msgIdList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorClass() {
        return errorClass;
    }

    public void setErrorClass(String errorClass) {
        this.errorClass = errorClass;
    }
}