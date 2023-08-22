package com.sohu.tv.mq.cloud.web.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description 取消消息发送状态统计
 * @date 2023/7/6 11:20:32
 */
public class WheelCancelMessageVo {

    private int expiredCancelMsgNum = 0;

    private int failedCancelMsgNum = 0;

    private int successCancelMsgNum = 0;

    private int updateDbFailedCancelMsgNum = 0;

    private List<cancelResult> detail = new ArrayList<>();

    public void incrExpiredCancelMsgNum() {
        ++this.expiredCancelMsgNum;
    }

    public void incrExpiredCancelMsgNum(int num) {
        this.expiredCancelMsgNum += num;
    }

    public void incrFailedCancelMsgNum() {
        ++this.failedCancelMsgNum;
    }

    public void incrSuccessCancelMsgNum() {
        ++this.successCancelMsgNum;
    }

    public void incrUpdateDbFailedCancelMsgNum() {
        ++this.updateDbFailedCancelMsgNum;
    }

    public void addDetail(String uniqId, String statusDesc) {
        this.detail.add(new cancelResult(uniqId, statusDesc));
    }

    public int getExpiredCancelMsgNum() {
        return expiredCancelMsgNum;
    }

    public void setExpiredCancelMsgNum(int expiredCancelMsgNum) {
        this.expiredCancelMsgNum = expiredCancelMsgNum;
    }

    public int getFailedCancelMsgNum() {
        return failedCancelMsgNum;
    }

    public void setFailedCancelMsgNum(int failedCancelMsgNum) {
        this.failedCancelMsgNum = failedCancelMsgNum;
    }

    public int getSuccessCancelMsgNum() {
        return successCancelMsgNum;
    }

    public void setSuccessCancelMsgNum(int successCancelMsgNum) {
        this.successCancelMsgNum = successCancelMsgNum;
    }

    public int getUpdateDbFailedCancelMsgNum() {
        return updateDbFailedCancelMsgNum;
    }

    public void setUpdateDbFailedCancelMsgNum(int updateDbFailedCancelMsgNum) {
        this.updateDbFailedCancelMsgNum = updateDbFailedCancelMsgNum;
    }

    public List<cancelResult> getDetail() {
        return detail;
    }

    public void setDetail(List<cancelResult> detail) {
        this.detail = detail;
    }

    public boolean sendAllOk() {
        return expiredCancelMsgNum == 0 && failedCancelMsgNum == 0 && updateDbFailedCancelMsgNum == 0;
    }

    public class cancelResult{

        public String uniqId;

        public String statusDesc;

        public cancelResult(String uniqId, String statusDesc) {
            this.uniqId = uniqId;
            this.statusDesc = statusDesc;
        }

        public String getUniqId() {
            return uniqId;
        }

        public void setUniqId(String uniqId) {
            this.uniqId = uniqId;
        }

        public String getStatusDesc() {
            return statusDesc;
        }

        public void setStatusDesc(String statusDesc) {
            this.statusDesc = statusDesc;
        }
    }
}
