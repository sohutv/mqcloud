package com.sohu.tv.mq.cloud.bo;

/**
 * 导出的消息
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/26
 */
public class ExportedMessage {
    private String msgId;
    private long timestamp;
    private String body;

    public ExportedMessage() {
    }

    public ExportedMessage(String msgId, long timestamp, String body) {
        this.msgId = msgId;
        this.timestamp = timestamp;
        this.body = body;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ExportedMessage{" +
                "msgId='" + msgId + '\'' +
                ", timestamp=" + timestamp +
                ", body='" + body + '\'' +
                '}';
    }
}
