package com.sohu.tv.mq.dto;

public class AbstractResult {
    public static final int OK_STATUS = 200;
    // 请求状态
    protected int status;
    // 提示信息
    protected String message;
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean ok() {
        return OK_STATUS == status;
    }
}
