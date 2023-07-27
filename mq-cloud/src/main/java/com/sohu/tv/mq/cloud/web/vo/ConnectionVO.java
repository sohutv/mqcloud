package com.sohu.tv.mq.cloud.web.vo;

import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.remoting.protocol.body.Connection;

public class ConnectionVO {
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    public String versionDesc() {
        return MQVersion.getVersionDesc(connection.getVersion());
    }

    @Override
    public String toString() {
        return "ConnectionVO [connection=" + connection + ", versionDesc()=" + versionDesc() + "]";
    }
}
