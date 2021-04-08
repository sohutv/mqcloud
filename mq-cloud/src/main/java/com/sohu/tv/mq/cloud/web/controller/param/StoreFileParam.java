package com.sohu.tv.mq.cloud.web.controller.param;

import com.sohu.tv.mq.cloud.bo.StoreFiles.StoreFile;

/**
 * 存储文件参数
 * 
 * @author yongfeigao
 * @date 2020年12月1日
 */
public class StoreFileParam {
    private String sourceIp;
    private String sourceHome;
    private String destIp;
    private String destHome;
    private StoreFile storeFile;

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getSourceHome() {
        return sourceHome;
    }

    public void setSourceHome(String sourceHome) {
        this.sourceHome = sourceHome;
    }

    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public String getDestHome() {
        return destHome;
    }

    public void setDestHome(String destHome) {
        this.destHome = destHome;
    }

    public StoreFile getStoreFile() {
        return storeFile;
    }

    public void setStoreFile(StoreFile storeFile) {
        this.storeFile = storeFile;
    }
}
