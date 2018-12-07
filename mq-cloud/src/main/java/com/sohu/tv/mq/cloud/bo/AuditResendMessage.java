package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import com.sohu.tv.mq.cloud.util.DateUtil;

/**
 * 消息重发
 * 
 * @author yongfeigao
 * @date 2018年12月6日
 */
public class AuditResendMessage {
    private long aid;
    private long tid;
    // broker offset msg id
    private String msgId;
    // 申请类型:0:未处理,1:发送成功,2:发送失败
    private int status;
    // 发送次数
    private int times;
    // 发送时间
    private Date sendTime;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public int getStatus() {
        return status;
    }
    
    public String getStatusDesc() {
        return StatusEnum.getEnumByStatus(status).getDesc();
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public Date getSendTime() {
        return sendTime;
    }
    
    public String getSendTimeFormat() {
        return DateUtil.getFormat(DateUtil.YMD_BLANK_HMS_COLON).format(sendTime);
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    // 审批状态
    public enum StatusEnum {

        INIT(0, "未处理"), 
        SUCCESS(1, "成功"), 
        FAILED(2, "失败");

        private int status;
        private String desc;

        StatusEnum(int status, String desc) {
            this.status = status;
            this.desc = desc;
        }

        public int getStatus() {
            return status;
        }

        public String getDesc() {
            return desc;
        }

        public static StatusEnum getEnumByStatus(int status) {
            for (StatusEnum statusEnum : StatusEnum.values()) {
                if (statusEnum.status == status) {
                    return statusEnum;
                }
            }
            return StatusEnum.INIT;
        }
    }
}
