package com.sohu.tv.mq.cloud.bo;

/**
 * 消费者暂停配置
 *
 * @author yongfeigao
 * @date 2023/12/12
 */
public class ConsumerPauseConfig {
    // 消费者
    private String consumer;
    private String pauseClientId;
    // 是否解注册
    private boolean unregister;

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getPauseClientId() {
        return pauseClientId;
    }

    public void setPauseClientId(String pauseClientId) {
        this.pauseClientId = pauseClientId;
    }

    public boolean getUnregister() {
        return unregister;
    }

    public void setUnregister(boolean unregister) {
        this.unregister = unregister;
    }

    @Override
    public String toString() {
        return "ConsumerPauseConfig{" +
                "consumer='" + consumer + '\'' +
                ", pauseClientId='" + pauseClientId + '\'' +
                ", unregister=" + unregister +
                '}';
    }
}
