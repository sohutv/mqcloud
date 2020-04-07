package com.sohu.tv.mq.cloud.bo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * 批量关联
 * 
 * @author yongfeigao
 * @date 2020年3月18日
 */
public class AuditBatchAssociate {
    // 审核id
    private long aid;
    // 用户id，逗号分隔
    private String uids;
    // 生产者id
    private String producerIds;
    // 消费者id
    private String consumerIds;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public String getUids() {
        return uids;
    }

    public void setUids(String uids) {
        this.uids = uids;
    }

    public List<Long> getUidList() {
        return splitToLongList(uids);
    }

    public List<Long> splitToLongList(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        String[] strs = str.split(",");
        List<Long> list = new ArrayList<Long>();
        for (String s : strs) {
            list.add(NumberUtils.toLong(s));
        }
        return list;
    }

    public String getProducerIds() {
        return producerIds;
    }

    public void setProducerIds(String producerIds) {
        this.producerIds = producerIds;
    }

    public List<Long> getProducerIdList() {
        return splitToLongList(producerIds);
    }

    public String getConsumerIds() {
        return consumerIds;
    }

    public void setConsumerIds(String consumerIds) {
        this.consumerIds = consumerIds;
    }

    public List<Long> getConsumerIdList() {
        return splitToLongList(consumerIds);
    }

    @Override
    public String toString() {
        return "AuditBatchAssociate [aid=" + aid + ", uids=" + uids + ", producerIds=" + producerIds + ", consumerIds="
                + consumerIds + "]";
    }
}