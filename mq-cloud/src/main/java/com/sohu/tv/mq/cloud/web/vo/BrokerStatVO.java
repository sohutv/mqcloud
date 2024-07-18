package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.util.MessageDelayLevel;
import com.sohu.tv.mq.cloud.util.WebUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * broker状态
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月9日
 */
public class BrokerStatVO {
    // 地址
    private String brokerAddr;
    // 版本
    private String version;
    // 生成量
    private String inTps;
    // 消息量
    private String outTps;
    // 监控结果 正常-异常
    private int checkStatus;
    // 监控时间
    private String checkTime;

    private Map<String, String> info;

    private DelayQueue delayQueue;
    
    private long commitLogMaxOffset;

    private String baseDir;

    // 是否可以写入
    private boolean writable = true;

    private String brokerId;

    private Date createTime;

    private long fallbehindSize;

    /**
     * 时间轮生产流程
     * 1.TimerEnqueueGetService从rmq_sys_wheel_timer拉取消息,封装TimerRequest写入enqueuePutQueue
     * 2.TimerEnqueuePutService从enqueuePutQueue拉取TimerRequest,封装TimerLog绑定时间轮
     * 时间轮消费流程
     * 1.TimerDequeueGetService从时间轮拉取TimerLog,封装TimerRequest写入dequeueGetQueue
     * 2.TimerDequeueGetMessageService从dequeueGetQueue拉取TimerRequest,获取真实消息写入dequeuePutQueue
     * 3.TimerDequeuePutMessageService从dequeuePutQueue拉取消息转换为原始消息投入原始队列
     */
    // 从enqueuePutQueue写入时间轮，表示数据写入时间轮的吞吐
    private float timerEnqueueTps;
    // 从dequeuePutQueue写入原始topic，表示消息写入原始topic的吞吐
    private float timerDequeueTps;
    // rmq_sys_wheel_timer还有多少消息未入queue，表示拉取rmq_sys_wheel_timer消息的落后量
    private long timerOffsetBehind;
    // TimerLog入dequeueGetQueue的落后时间，表示消费时间轮的落后时间
    private long timerReadBehind;
    // 时间轮还有多少消息，表示时间轮有多少消息未到时
    private long timerCongestNum;

    public Map<String, String> getInfo() {
        return info;
    }

    public void setInfo(Map<String, String> info) {
        this.info = info;
    }

    public String getBrokerAddr() {
        return brokerAddr;
    }

    public String getBrokerIp() {
        return brokerAddr.split(":")[0];
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    public String getInTps() {
        return inTps;
    }

    public void setInTps(String inTps) {
        this.inTps = inTps;
    }

    public String getOutTps() {
        return outTps;
    }

    public void setOutTps(String outTps) {
        this.outTps = outTps;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }

    public DelayQueue getDelayQueue() {
        return delayQueue;
    }

    public long getCommitLogMaxOffset() {
        return commitLogMaxOffset;
    }

    public void setCommitLogMaxOffset(long commitLogMaxOffset) {
        this.commitLogMaxOffset = commitLogMaxOffset;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public void addDelayMessageOffset(MessageDelayLevel messageDelayLevel, long curOffset, long maxOffset) {
        if (delayQueue == null) {
            delayQueue = new DelayQueue();
        }
        delayQueue.addDelayMessageOffset(messageDelayLevel, curOffset, maxOffset);
    }

    public String getCheckStatusDesc() {
        return CheckStatusEnum.getCheckStatusEnumByStatus(getCheckStatus()).getDesc();
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public float getTimerEnqueueTps() {
        return timerEnqueueTps;
    }

    public String getTimerEnqueueTpsFormat() {
        return WebUtil.format(timerEnqueueTps);
    }

    public void setTimerEnqueueTps(float timerEnqueueTps) {
        this.timerEnqueueTps = timerEnqueueTps;
    }

    public float getTimerDequeueTps() {
        return timerDequeueTps;
    }

    public String getTimerDequeueTpsFormat() {
        return WebUtil.format(timerDequeueTps);
    }

    public void setTimerDequeueTps(float timerDequeueTps) {
        this.timerDequeueTps = timerDequeueTps;
    }

    public long getTimerOffsetBehind() {
        return timerOffsetBehind;
    }

    public void setTimerOffsetBehind(long timerOffsetBehind) {
        this.timerOffsetBehind = timerOffsetBehind;
    }

    public long getTimerReadBehind() {
        return timerReadBehind;
    }

    public void setTimerReadBehind(long timerReadBehind) {
        this.timerReadBehind = timerReadBehind;
    }

    public long getTimerCongestNum() {
        return timerCongestNum;
    }

    public void setTimerCongestNum(long timerCongestNum) {
        this.timerCongestNum = timerCongestNum;
    }

    public String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getFallbehindSize() {
        return fallbehindSize;
    }

    public String getFallbehindSizeFormat() {
        return WebUtil.sizeFormat(fallbehindSize);
    }

    public void setFallbehindSize(long fallbehindSize) {
        this.fallbehindSize = fallbehindSize;
    }

    public boolean isMaster(){
        return "0".endsWith(brokerId);
    }

    public class DelayQueue {
        private List<DelayQueueOffset> delayQueueOffsetList;
        private long delayMessageOffset;

        public void addDelayMessageOffset(MessageDelayLevel messageDelayLevel, long curOffset, long maxOffset) {
            if (delayQueueOffsetList == null) {
                delayQueueOffsetList = new ArrayList<>();
            }
            delayQueueOffsetList.add(new DelayQueueOffset(messageDelayLevel, curOffset, maxOffset));
            delayMessageOffset += maxOffset - curOffset;
        }

        public List<DelayQueueOffset> getDelayQueueOffsetList() {
            return delayQueueOffsetList;
        }

        public void setDelayQueueOffsetList(List<DelayQueueOffset> delayQueueOffsetList) {
            this.delayQueueOffsetList = delayQueueOffsetList;
        }

        public long getDelayMessageOffset() {
            return delayMessageOffset;
        }

        public void setDelayMessageOffset(long delayMessageOffset) {
            this.delayMessageOffset = delayMessageOffset;
        }
    }

    public class DelayQueueOffset {
        private MessageDelayLevel messageDelayLevel;
        private long curOffset;
        private long maxOffset;

        public DelayQueueOffset(MessageDelayLevel messageDelayLevel, long curOffset, long maxOffset) {
            this.messageDelayLevel = messageDelayLevel;
            this.curOffset = curOffset;
            this.maxOffset = maxOffset;
        }

        public MessageDelayLevel getMessageDelayLevel() {
            return messageDelayLevel;
        }

        public void setMessageDelayLevel(MessageDelayLevel messageDelayLevel) {
            this.messageDelayLevel = messageDelayLevel;
        }

        public long getCurOffset() {
            return curOffset;
        }

        public void setCurOffset(long curOffset) {
            this.curOffset = curOffset;
        }

        public long getMaxOffset() {
            return maxOffset;
        }

        public void setMaxOffset(long maxOffset) {
            this.maxOffset = maxOffset;
        }
    }
}
