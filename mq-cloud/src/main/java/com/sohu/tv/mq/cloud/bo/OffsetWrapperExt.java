package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;

/**
 * @author: yongfeigao
 * @date: 2022/6/7 16:51
 */
public class OffsetWrapperExt extends OffsetWrapper {
    private long lockTimestamp;
    private String clientIp;

    public long getLockTimestamp() {
        return lockTimestamp;
    }

    public void setLockTimestamp(long lockTimestamp) {
        this.lockTimestamp = lockTimestamp;
    }

    public String getLockTime() {
        if (lockTimestamp == 0) {
            return "0";
        }
        long time = System.currentTimeMillis() - lockTimestamp;
        return time / 1000 + "." + time % 1000;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
