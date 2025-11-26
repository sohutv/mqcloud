package com.sohu.tv.mq.cloud.bo;

/**
 * Proxy
 *
 * @author yongfeigao
 * @date 2023年05月25日
 */
public class Proxy extends DeployableComponent {
    // 配置项
    private String config;

    // 客户端链接数量
    private int producerSize;
    private int consumerSize;
    private int producerConnectionSize;
    private int consumerConnectionSize;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public int getProducerSize() {
        return producerSize;
    }

    public void setProducerSize(int producerSize) {
        this.producerSize = producerSize;
    }

    public int getConsumerSize() {
        return consumerSize;
    }

    public void setConsumerSize(int consumerSize) {
        this.consumerSize = consumerSize;
    }

    public int getProducerConnectionSize() {
        return producerConnectionSize;
    }

    public void setProducerConnectionSize(int producerConnectionSize) {
        this.producerConnectionSize = producerConnectionSize;
    }

    public int getConsumerConnectionSize() {
        return consumerConnectionSize;
    }

    public void setConsumerConnectionSize(int consumerConnectionSize) {
        this.consumerConnectionSize = consumerConnectionSize;
    }

    @Override
    public String getComponentName() {
        return "proxy";
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "config='" + config + '\'' +
                "} " + super.toString();
    }
}
