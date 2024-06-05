package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * Proxy
 *
 * @author yongfeigao
 * @date 2023年05月25日
 */
public class Proxy extends DeployableComponent {
    // 配置项
    private String config;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
    @Override
    public String toString() {
        return "Proxy{" +
                "config='" + config + '\'' +
                "} " + super.toString();
    }
}
