package com.sohu.tv.mq.cloud.web.vo;

import java.util.Map;

import com.sohu.tv.mq.cloud.common.util.WebUtil;

public class ScpDirVO {
    
    private boolean md5OK;
    
    private boolean sizeOK;
    
    private long use = 1;
    
    private long size;
    
    private Map<String, ScpVO> scpVOMap;
    
    public ScpDirVO(boolean md5ok, boolean sizeOK, long use, long size, Map<String, ScpVO> scpVOMap) {
        md5OK = md5ok;
        this.sizeOK = sizeOK;
        if(use > 0) {
            this.use = use;
        }
        this.size = size;
        this.scpVOMap = scpVOMap;
    }

    public boolean isSizeOK() {
        return sizeOK;
    }

    public boolean isMd5OK() {
        return md5OK;
    }

    public Map<String, ScpVO> getScpVOMap() {
        return scpVOMap;
    }
    
    public long getSize() {
        return size;
    }

    public String getHumanReadableRate() {
        double bytePerSec = size / use * 1000;
        return WebUtil.sizeFormat((long) bytePerSec) + "/s";
    }
}
