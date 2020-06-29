package com.sohu.tv.mq.cloud.bo;

/**
 * 集群配置
 * 
 * @author yongfeigao
 * @date 2020年5月20日
 */
public class ClusterConfig {
    private int cid;
    // broker config id
    private int bid;
    // 线上值
    private String onlineValue;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public String getOnlineValue() {
        return onlineValue;
    }

    public void setOnlineValue(String onlineValue) {
        this.onlineValue = onlineValue;
    }

    @Override
    public String toString() {
        return "ClusterConfig [cid=" + cid + ", bid=" + bid + ", onlineValue=" + onlineValue + "]";
    }
}
