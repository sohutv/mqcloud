package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.remoting.protocol.body.Connection;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: connnection包装类
 * @date 2022/5/5 9:27
 */
public class VersionConnection extends Connection {

    // 版本号字符串
    private String versionStr;

    public VersionConnection() {
    }

    public VersionConnection(Connection connection,String versionStr) {
        setVersion(connection.getVersion());
        setLanguage(connection.getLanguage());
        setClientAddr(connection.getClientAddr());
        setClientId(connection.getClientId());
        this.versionStr = versionStr;
    }

    public String getVersionStr() {
        return versionStr;
    }

    public void setVersionStr(String versionStr) {
        this.versionStr = versionStr;
    }
}
