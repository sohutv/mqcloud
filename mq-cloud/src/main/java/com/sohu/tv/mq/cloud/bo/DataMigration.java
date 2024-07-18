package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.WebUtil;

import java.util.Date;

/**
 * 数据迁移
 *
 * @author yongfeigao
 * @date 2024年07月03日
 */
public class DataMigration {
    public static final int STATUS_NOT_START = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_FINISH = 2;
    private long id;
    private String sourceIp;
    private String sourcePath;
    private String destIp;
    private String destPath;
    private long dataCount;
    private long costTime;
    private Date createTime;
    private Date updateTime;
    // 0:未开始 1:进行中 2:完成
    private int status;
    private String info;

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public String getDestPath() {
        return destPath;
    }

    public void setDestPath(String destPath) {
        this.destPath = destPath;
    }

    public long getDataCount() {
        return dataCount;
    }

    public String getDataCountDesc() {
        return WebUtil.sizeFormat(dataCount);
    }

    public void setDataCount(long dataCount) {
        this.dataCount = dataCount;
    }

    public long getCostTime() {
        return costTime;
    }

    public String getCostTimeDesc() {
        return WebUtil.timeFormat(costTime);
    }

    public void setCostTime(long costTime) {
        this.costTime = costTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public String getCreateTimeDesc() {
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(createTime);
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setRunning() {
        this.status = STATUS_RUNNING;
        this.info = "running...";
    }

    public void setFinish() {
        setFinish("finished");
    }

    public void setFinish(String info) {
        this.status = STATUS_FINISH;
        this.info = info;
        if (updateTime != null) {
            this.costTime = System.currentTimeMillis() - updateTime.getTime();
        }
    }

    public boolean isRunning() {
        return status == STATUS_RUNNING;
    }

    public boolean isFinish() {
        return status == STATUS_FINISH;
    }

    public String getStatusDesc() {
        switch (status) {
            case STATUS_NOT_START:
                return "未开始";
            case STATUS_RUNNING:
                return "进行中";
            case STATUS_FINISH:
                return "完成";
            default:
                return "未知";
        }
    }

    @Override
    public String toString() {
        return "DataMigration{" +
                "id=" + id +
                ", sourceIp='" + sourceIp + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                ", destIp='" + destIp + '\'' +
                ", destPath='" + destPath + '\'' +
                ", dataCount=" + dataCount +
                ", costTime=" + costTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", status=" + status +
                ", info='" + info + '\'' +
                '}';
    }
}
