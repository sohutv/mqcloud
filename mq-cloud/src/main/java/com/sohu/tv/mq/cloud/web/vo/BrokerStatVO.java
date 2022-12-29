package com.sohu.tv.mq.cloud.web.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.util.MessageDelayLevel;
import com.sohu.tv.mq.cloud.util.WebUtil;

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

    public Map<String, String> getInfo() {
        return info;
    }

    public void setInfo(Map<String, String> info) {
        this.info = info;
    }

    public String getBrokerAddr() {
        return brokerAddr;
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
    
    public String format(long size) {
        return WebUtil.sizeFormat(size);
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
