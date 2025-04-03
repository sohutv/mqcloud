package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.util.DateUtil;

import java.util.Date;

/**
 * 可部署的组件
 *
 * @date 2024年05月32日
 */
public abstract class DeployableComponent {
    // cluster id
    protected int cid;
    // ip:port
    protected String addr;
    // 安装路径
    protected String baseDir;
    // 创建时间
    protected Date createTime;
    // 检测状态
    protected int checkStatus;
    // 检测时间
    protected Date checkTime;
    // 状态
    protected int status;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getAddr() {
        return addr;
    }

    public String getIp() {
        return addr.split(":")[0];
    }

    public int getPort() {
        return Integer.parseInt(addr.split(":")[1]);
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public boolean isCheckStatusOK() {
        return checkStatus == CheckStatusEnum.OK.getStatus();
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public String getCreateTimeFormat() {
        if(getCreateTime() == null) {
            return null;
        }
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(getCreateTime());
    }

    public String getCheckTimeFormat() {
        if(getCheckTime() == null) {
            return "";
        }
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(getCheckTime());
    }

    public String getCheckStatusDesc() {
        return CheckStatusEnum.getCheckStatusEnumByStatus(getCheckStatus()).getDesc();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isStatusOK() {
        return status == 0;
    }

    @Override
    public String toString() {
        return "{" +
                "cid=" + cid +
                ", addr='" + addr + '\'' +
                ", baseDir='" + baseDir + '\'' +
                ", createTime=" + createTime +
                ", checkStatus=" + checkStatus +
                ", checkTime=" + checkTime +
                ", status=" + status +
                '}';
    }
}
