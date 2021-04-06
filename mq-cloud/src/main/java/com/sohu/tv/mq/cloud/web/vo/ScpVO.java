package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.util.WebUtil;

/**
 * scp结果
 * 
 * @author yongfeigao
 * @date 2020年11月27日
 */
public class ScpVO {
    private String sourceMD5;
    private String destMD5;
    private long use = 1;
    private long sourceSize;
    private long destSize;

    public ScpVO(String sourceMD5, String destMD5, long use, long sourceSize, long destSize) {
        this.sourceMD5 = sourceMD5;
        this.destMD5 = destMD5;
        if(use > 0) {
            this.use = use;
        }
        this.sourceSize = sourceSize;
        this.destSize = destSize;
    }

    public boolean isMD5OK() {
        return sourceMD5.equals(destMD5);
    }
    
    public boolean isSizeOK() {
        return sourceSize == destSize;
    }

    public String getSourceMD5() {
        return sourceMD5;
    }

    public void setSourceMD5(String sourceMD5) {
        this.sourceMD5 = sourceMD5;
    }

    public String getDestMD5() {
        return destMD5;
    }

    public void setDestMD5(String destMD5) {
        this.destMD5 = destMD5;
    }

    public long getSourceSize() {
        return sourceSize;
    }
    
    public long getSize() {
        return sourceSize;
    }

    public void setSourceSize(long sourceSize) {
        this.sourceSize = sourceSize;
    }

    public long getDestSize() {
        return destSize;
    }

    public void setDestSize(long destSize) {
        this.destSize = destSize;
    }

    public String getHumanReadableRate() {
        double bytePerSec = sourceSize / use * 1000;
        return WebUtil.sizeFormat((long) bytePerSec) + "/s";
    }
}
