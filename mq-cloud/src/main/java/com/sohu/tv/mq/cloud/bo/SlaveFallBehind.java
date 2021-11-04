package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.util.WebUtil;

/**
 * slave落后
 * 
 * @author yongfeigao
 * @date 2021年9月22日
 */
public class SlaveFallBehind {
    private String clusterName;
    private String brokerLink;
    private long fallBehindOffset;
    private long slaveFallBehindSize;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getBrokerLink() {
        return brokerLink;
    }

    public void setBrokerLink(String brokerLink) {
        this.brokerLink = brokerLink;
    }

    public long getFallBehindOffset() {
        return fallBehindOffset;
    }
    
    public String getFallBehindOffsetFormat() {
        return WebUtil.sizeFormat(fallBehindOffset);
    }

    public void setFallBehindOffset(long fallBehindOffset) {
        this.fallBehindOffset = fallBehindOffset;
    }

    public long getSlaveFallBehindSize() {
        return slaveFallBehindSize;
    }
    
    public String getSlaveFallBehindSizeFormat() {
        return WebUtil.sizeFormat(slaveFallBehindSize);
    }

    public void setSlaveFallBehindSize(long slaveFallBehindSize) {
        this.slaveFallBehindSize = slaveFallBehindSize;
    }
}
