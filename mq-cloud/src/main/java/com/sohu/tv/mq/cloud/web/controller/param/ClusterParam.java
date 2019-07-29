package com.sohu.tv.mq.cloud.web.controller.param;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
/**
 * cluster参数
 * 
 * @author yongfeigao
 * @date 2018年10月17日
 */
public class ClusterParam {
    
    private int id;
    
    @NotBlank
    private String name;
    
    @Range(min = 0, max = 1)
    private int vipChannelEnabled;

    @Range(min = 0, max = 1)
    private int online;
    
    @Range(min = 0, max = 1)
    private int transactionEnabled;
    
    @Range(min = 0, max = 1)
    private int traceEnabled;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVipChannelEnabled() {
        return vipChannelEnabled;
    }

    public void setVipChannelEnabled(int vipChannelEnabled) {
        this.vipChannelEnabled = vipChannelEnabled;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public int getTransactionEnabled() {
        return transactionEnabled;
    }

    public void setTransactionEnabled(int transactionEnabled) {
        this.transactionEnabled = transactionEnabled;
    }

    public int getTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(int traceEnabled) {
        this.traceEnabled = traceEnabled;
    }
}
