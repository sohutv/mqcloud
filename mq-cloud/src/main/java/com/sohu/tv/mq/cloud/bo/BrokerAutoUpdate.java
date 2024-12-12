package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.common.util.WebUtil;
import com.sohu.tv.mq.cloud.util.DateUtil;

import java.util.Date;

/**
 * Broker自动更新
 *
 * @author yongfeigao
 * @date 2024年10月31日
 */
public class BrokerAutoUpdate {
    // id
    private int id;
    // 集群id
    private int cid;
    // 状态
    private int status;
    // 创建时间
    private Date createTime;
    // 开始时间
    private Date startTime;
    // 更新时间
    private Date updateTime;

    public static enum Status {
        // 任务刚创建的初始状态
        INIT(1, "未开始", "text-info"),
        // 任务已经准备好，等待执行(需要手动触发)
        READY(2, "已就绪", "text-secondary"),
        // 任务正在执行，自动从READY状态转换为RUNNING状态，或者手动从PAUSE状态转换为RUNNING状态
        RUNNING(3, "进行中", "text-yellow"),
        // 任务暂停，手动从RUNNING状态转换为PAUSE状态
        PAUSE(4, "暂停中", "text-warning"),
        // 任务成功
        SUCCESS(5, "成功", "text-success"),
        // 任务失败
        FAILED(6, "失败", "text-danger"),
        FINISHED(7, "手动结束", "text-primary"),
        ;
        // 未完成状态
        public static final int[] UNDONE_STATUS = {INIT.getValue(), READY.getValue(), RUNNING.getValue(), PAUSE.getValue()};

        // 可执行状态
        public static final int[] EXECUTE_STATUS = {READY.getValue(), RUNNING.getValue()};

        private int value;
        private String desc;
        private String color;


        Status(int value, String desc, String color) {
            this.value = value;
            this.desc = desc;
            this.color = color;
        }

        public int getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }

        public String getColor() {
            return color;
        }

        public static Status valueOf(int value) {
            for (Status status : Status.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getStatus() {
        return status;
    }

    public boolean isInit() {
        return status == Status.INIT.getValue();
    }

    public boolean isReady() {
        return status == Status.READY.getValue();
    }

    public boolean isRunning() {
        return status == Status.RUNNING.getValue();
    }

    public boolean isPause() {
        return status == Status.PAUSE.getValue();
    }

    public boolean isOver() {
        return status == Status.SUCCESS.getValue() || status == Status.FAILED.getValue() ||
                status == Status.FINISHED.getValue();
    }

    public String getStatusDesc() {
        return Status.valueOf(status).getDesc();
    }

    public String getStatusColor() {
        return Status.valueOf(status).getColor();
    }

    public void setStatus(int status) {
        this.status = status;
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

    public String getUpdateTimeDesc() {
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(updateTime);
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCostTimeDesc() {
        if (startTime == null) {
            return null;
        }
        long cost = updateTime.getTime() - startTime.getTime();
        if (cost <= 0) {
            return null;
        }
        return WebUtil.timeFormat(updateTime.getTime() - startTime.getTime());
    }

    public Date getStartTime() {
        return startTime;
    }

    public String getStartTimeDesc() {
        return DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(startTime);
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "BrokerAutoUpdate{" +
                "id=" + id +
                ", cid=" + cid +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
