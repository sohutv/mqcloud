package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.AuditResendMessage;
/**
 * 重发消息VO
 * 
 * @author yongfeigao
 * @date 2018年12月6日
 */
public class AuditResendMessageVO {
    private String topic;
    private List<AuditResendMessage> msgList;
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public List<AuditResendMessage> getMsgList() {
        return msgList;
    }
    public void setMsgList(List<AuditResendMessage> msgList) {
        this.msgList = msgList;
    }
}
