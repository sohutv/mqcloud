package com.sohu.tv.mq.cloud.web.vo;
/**
 * 消息vo
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
public class MessageVO {
    private long id;
    private String text;
    private int readStatus;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public int getReadStatus() {
        return readStatus;
    }
    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }
}
