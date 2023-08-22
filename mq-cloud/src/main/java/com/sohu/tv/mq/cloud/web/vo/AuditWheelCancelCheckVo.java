package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.AuditWheelMessageCancel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description
 * @date 2023/7/6 16:09:59
 */
public class AuditWheelCancelCheckVo {

    private String topicName;

    private int validCancelNum;

    private List<CancelMsgApply> cancelMsgApplys = new ArrayList<>();

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getValidCancelNum() {
        return validCancelNum;
    }

    public void setValidCancelNum(int validCancelNum) {
        this.validCancelNum = validCancelNum;
    }


    public List<CancelMsgApply> getCancelMsgApplys() {
        return cancelMsgApplys;
    }

    public void setCancelMsgApplys(List<CancelMsgApply> cancelMsgApplys) {
        this.cancelMsgApplys = cancelMsgApplys;
    }

    public void addCancelMsgApply(CancelMsgApply cancelMsgApply) {
        this.cancelMsgApplys.add(cancelMsgApply);
    }

    public static class CancelMsgApply{
        private Integer index;
        private String uniqId;
        private String formatTime;
        private String status;

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getUniqId() {
            return uniqId;
        }

        public void setUniqId(String uniqId) {
            this.uniqId = uniqId;
        }

        public String getFormatTime() {
            return formatTime;
        }

        public void setFormatTime(long formatTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.formatTime = sdf.format(formatTime);
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(boolean noCancel) {
            this.status = noCancel ? "待取消" : "已取消";
        }
    }
}
