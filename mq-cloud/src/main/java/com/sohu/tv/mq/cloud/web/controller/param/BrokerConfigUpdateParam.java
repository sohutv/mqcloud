package com.sohu.tv.mq.cloud.web.controller.param;

import java.util.Properties;

/**
 * broker配置更新参数
 *
 * @author yongfeigao
 * @date 2024年4月17日
 */
public class BrokerConfigUpdateParam {
    // 集群id
    private int cid;
    // broker地址
    private String addr;
    // 配置参数
    private String config;
    // 是否集群
    private boolean cluster;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public boolean isCluster() {
        return cluster;
    }

    public void setCluster(boolean cluster) {
        this.cluster = cluster;
    }

    public Properties getConfigProperties() {
        Properties properties = new Properties();
        String[] configs = config.split(";");
        for (String cfg : configs) {
            String[] cfgs = cfg.split(":");
            properties.put(cfgs[0], cfgs[1]);
        }
        return properties;
    }

    @Override
    public String toString() {
        return "BrokerConfigUpdateParam{" +
                "cid=" + cid +
                ", addr='" + addr + '\'' +
                ", config='" + config + '\'' +
                ", cluster=" + cluster +
                '}';
    }
}
