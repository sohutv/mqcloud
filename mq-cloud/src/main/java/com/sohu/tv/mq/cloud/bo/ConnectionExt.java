package com.sohu.tv.mq.cloud.bo;

/**
 * @Auther: yongfeigao
 * @Date: 2023/12/12
 */
public class ConnectionExt {

    private String clientId;

    private boolean paused;

    public ConnectionExt() {
    }

    public ConnectionExt(String clientId) {
        this.clientId = clientId;
    }

    public ConnectionExt(String clientId, boolean paused) {
        this.clientId = clientId;
        this.paused = paused;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
