package com.sohu.tv.mq.cloud.bo;

import java.util.List;
/**
 * 消息数据
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月21日
 */
public class MessageData {
    private List<DecodedMessage> msgList;
    private MessageQueryCondition mqc;
    public List<DecodedMessage> getMsgList() {
        return msgList;
    }
    public void setMsgList(List<DecodedMessage> msgList) {
        this.msgList = msgList;
    }
    public MessageQueryCondition getMqc() {
        return mqc;
    }
    public void setMp(MessageQueryCondition mqc) {
        this.mqc = mqc;
    }
}
