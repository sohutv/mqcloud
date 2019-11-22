package com.sohu.tv.mq.dto;
/**
 * 消息重置
 * 
 * @author yongfeigao
 * @date 2019年10月28日
 */
public class MessageResetDTO {
    // 重置到
    private long resetTo;

    public long getResetTo() {
        return resetTo;
    }

    public void setResetTo(long resetTo) {
        this.resetTo = resetTo;
    }
}
