package com.sohu.tv.mq.cloud.common.model;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 查看瞬时数据请求头
 * 
 * @author yongfeigao
 * @date 2020年7月9日
 */
public class ViewMomentStatsDataRequestHeader implements CommandCustomHeader {
    
    @CFNotNull
    private String statsName;
    
    // 小于此值的无需返回
    private long minValue;

    @Override
    public void checkFields() throws RemotingCommandException {
        
    }

    public String getStatsName() {
        return statsName;
    }

    public void setStatsName(String statsName) {
        this.statsName = statsName;
    }

    public long getMinValue() {
        return minValue;
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }
}
