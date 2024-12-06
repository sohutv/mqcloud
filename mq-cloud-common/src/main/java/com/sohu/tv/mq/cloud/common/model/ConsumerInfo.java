package com.sohu.tv.mq.cloud.common.model;

import org.apache.rocketmq.remoting.protocol.LanguageCode;

/**
 * 消费者链接信息
 *
 * @author yongfeigao
 * @date 2024年10月25日
 */
public class ConsumerInfo {
    private String clientId;
    private String remoteIP;
    private LanguageCode language;
    private int version;
    private long lastUpdateTimestamp;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public LanguageCode getLanguage() {
        return language;
    }

    public void setLanguage(LanguageCode language) {
        this.language = language;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    @Override
    public String toString() {
        return String.format("clientId=%s,remoteIP=%s, language=%s, version=%d, lastUpdateTimestamp=%d",
                clientId, remoteIP, language.name(), version, lastUpdateTimestamp);
    }
}
