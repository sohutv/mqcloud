package com.sohu.tv.mq.cloud.bo;

import org.apache.commons.lang3.StringUtils;

/**
 * 通用配置
 * 
 * @author yongfeigao
 * @date 2018年10月16日
 */
public class CommonConfig {
    // id
    private long id;
    
    // 配置key
    private String key;
    
    // 配置值
    private String value;
    
    // 备注
    private String comment;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isJsonValue() {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return key.equals("operatorContact") || key.equals("classList") || key.equals("mapWithByteList")
                || key.equals("machineRoom") || key.equals("machineRoomList") || key.equals("machineRoomColor")
                || key.equals("clientGroupNSConfig") || key.equals("oldReqestCodeBrokerSet") || key.equals("proxyAcls")
                || key.equals("orderTopicKVConfig") || key.equals("rsyncConfig") || key.equals("clusterStoreWarnConfig")
                ;
    }

    @Override
    public String toString() {
        return "CommonConfig [id=" + id + ", key=" + key + ", value=" + value + ", comment=" + comment + "]";
    }
}
