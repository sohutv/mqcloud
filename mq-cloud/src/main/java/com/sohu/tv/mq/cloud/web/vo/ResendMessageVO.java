package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.AuditResendMessage;

/**
 * 重发消息统计
 * 
 * @author yongfeigao
 * @date 2018年12月7日
 */
public class ResendMessageVO {
    // 总量
    private int total;
    // 发送成功量
    private int success;
    // 发送失败量
    private int failed;
    // 状态更新失败量
    private int statusUpdatedFailed;
    
    private List<AuditResendMessage> msgList;
    
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public int getSuccess() {
        return success;
    }
    public void incrSuccess() {
        ++this.success;
    }
    public void setSuccess(int success) {
        this.success = success;
    }
    public int getFailed() {
        return failed;
    }
    public void setFailed(int failed) {
        this.failed = failed;
    }
    public void incrFailed() {
        ++this.failed;
    }
    public int getStatusUpdatedFailed() {
        return statusUpdatedFailed;
    }
    public void setStatusUpdatedFailed(int statusUpdatedFailed) {
        this.statusUpdatedFailed = statusUpdatedFailed;
    }
    
    public void incrStatusUpdatedFailed() {
        ++this.statusUpdatedFailed;
    }
    
    public boolean sendAllOK() {
        return total == success && statusUpdatedFailed == 0;
    }
    public List<AuditResendMessage> getMsgList() {
        return msgList;
    }
    public void setMsgList(List<AuditResendMessage> msgList) {
        this.msgList = msgList;
    }
}
