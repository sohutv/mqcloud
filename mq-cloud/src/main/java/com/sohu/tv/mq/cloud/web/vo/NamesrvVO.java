package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;
/**
 * namesrv vo
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月13日
 */
public class NamesrvVO {
    private String nsUrl;
    private List<String> nsList;
    public String getNsUrl() {
        return nsUrl;
    }
    public void setNsUrl(String nsUrl) {
        this.nsUrl = nsUrl;
    }
    public List<String> getNsList() {
        return nsList;
    }
    public void setNsList(List<String> nsList) {
        this.nsList = nsList;
    }
}
