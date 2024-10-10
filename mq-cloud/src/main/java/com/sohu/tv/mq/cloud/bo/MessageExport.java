package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.common.util.WebUtil;

import java.util.Date;

/**
 * 消息导出
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/25
 */
public class MessageExport {
    private long aid;
    // 执行导出任务的机器
    private String ip;
    // 总消息量
    private long totalMsgCount;
    // 已导出消息量
    private long exportedMsgCount;
    // 剩余时间，单位毫秒
    private long leftTime;
    // 导出花费的时间，单位毫秒
    private long exportCostTime;
    // 压缩花费的时间，单位毫秒
    private long compressCostTime;
    // scp花费的时间，单位毫秒
    private long scpCostTime;
    // 导出文件路径
    private String exportedFilePath;
    // 导出的信息，包含错误信息
    private String info;
    private Date createTime;
    private Date updateTime;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getTotalMsgCount() {
        return totalMsgCount;
    }

    public void setTotalMsgCount(long totalMsgCount) {
        this.totalMsgCount = totalMsgCount;
    }

    public long getExportedMsgCount() {
        return exportedMsgCount;
    }

    public void setExportedMsgCount(long exportedMsgCount) {
        this.exportedMsgCount = exportedMsgCount;
    }

    public long getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(long leftTime) {
        this.leftTime = leftTime;
    }

    public long getExportCostTime() {
        return exportCostTime;
    }

    public void setExportCostTime(long exportCostTime) {
        this.exportCostTime = exportCostTime;
    }

    public long getCompressCostTime() {
        return compressCostTime;
    }

    public void setCompressCostTime(long compressCostTime) {
        this.compressCostTime = compressCostTime;
    }

    public long getScpCostTime() {
        return scpCostTime;
    }

    public void setScpCostTime(long scpCostTime) {
        this.scpCostTime = scpCostTime;
    }

    public String getExportedFilePath() {
        return exportedFilePath;
    }

    public void setExportedFilePath(String exportedFilePath) {
        this.exportedFilePath = exportedFilePath;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public String getUpdateTimeFormat() {
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(updateTime);
    }

    /**
     * 获取更新时间的延迟，分钟
     *
     * @return
     */
    public String getUpdateTimeDelayFormat() {
        long delay = System.currentTimeMillis() - updateTime.getTime();
        return WebUtil.timeFormat(delay);
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getExportedProcess() {
        if (exportCostTime > 0) {
            return "100%";
        }
        if (totalMsgCount == 0) {
            return "0%";
        }
        return String.format("%.2f", exportedMsgCount * 100.0 / totalMsgCount) + "%";
    }

    @Override
    public String toString() {
        return "MessageExport{" +
                "aid=" + aid +
                ", ip='" + ip + '\'' +
                ", totalMsgCount=" + totalMsgCount +
                ", exportedMsgCount=" + exportedMsgCount +
                ", leftTime=" + leftTime +
                ", exportCostTime=" + exportCostTime +
                ", compressCostTime=" + compressCostTime +
                ", scpCostTime=" + scpCostTime +
                ", exportedFilePath='" + exportedFilePath + '\'' +
                ", info='" + info + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
