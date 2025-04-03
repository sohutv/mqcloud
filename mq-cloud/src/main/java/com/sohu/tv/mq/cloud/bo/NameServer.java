package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * NameServer
 *
 * @author yongfeigao
 * @date 2018年10月23日
 */
public class NameServer extends DeployableComponent {
    // 链接数量
    private int connectionCount;

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    @Override
    public String toString() {
        return "NameServer " + super.toString();
    }
}
