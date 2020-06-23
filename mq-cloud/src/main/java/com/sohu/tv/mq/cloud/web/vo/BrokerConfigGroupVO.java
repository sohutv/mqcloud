package com.sohu.tv.mq.cloud.web.vo;

import java.util.ArrayList;
import java.util.List;

import com.sohu.tv.mq.cloud.bo.BrokerConfig;
import com.sohu.tv.mq.cloud.bo.BrokerConfigGroup;
/**
 * broker配置组
 * 
 * @author yongfeigao
 * @date 2020年5月19日
 */
public class BrokerConfigGroupVO {
    private BrokerConfigGroup brokerConfigGroup;
    private List<BrokerConfig> brokerConfigList;

    public BrokerConfigGroupVO(BrokerConfigGroup brokerConfigGroup) {
        this.brokerConfigGroup = brokerConfigGroup;
    }

    public void add(BrokerConfig brokerConfig) {
        if (brokerConfigList == null) {
            brokerConfigList = new ArrayList<>();
        }
        brokerConfigList.add(brokerConfig);
    }

    public BrokerConfigGroup getBrokerConfigGroup() {
        return brokerConfigGroup;
    }

    public void setBrokerConfigGroup(BrokerConfigGroup brokerConfigGroup) {
        this.brokerConfigGroup = brokerConfigGroup;
    }

    public List<BrokerConfig> getBrokerConfigList() {
        return brokerConfigList;
    }

    public void setBrokerConfigList(List<BrokerConfig> brokerConfigList) {
        this.brokerConfigList = brokerConfigList;
    }
}
