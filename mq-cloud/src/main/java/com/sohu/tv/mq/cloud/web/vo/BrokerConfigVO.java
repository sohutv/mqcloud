package com.sohu.tv.mq.cloud.web.vo;

import java.util.ArrayList;
import java.util.Collection;

import com.sohu.tv.mq.cloud.bo.BrokerConfig;
import com.sohu.tv.mq.cloud.bo.BrokerConfigGroup;

/**
 * broker配置VO
 * 
 * @author yongfeigao
 * @date 2020年5月18日
 */
public class BrokerConfigVO {
    private Collection<BrokerConfigGroupVO> brokerConfigGroups;
    private BrokerConfigGroupVO unknownBrokerConfigGroup;

    public void addUnknowItem(String key, String value) {
        if (unknownBrokerConfigGroup == null) {
            BrokerConfigGroup brokerConfigGroup = new BrokerConfigGroup();
            brokerConfigGroup.setGroup("未知项");
            unknownBrokerConfigGroup = new BrokerConfigGroupVO(brokerConfigGroup);
        }
        BrokerConfig brokerConfig = new BrokerConfig();
        brokerConfig.setKey(key);
        brokerConfig.setOnlineValue(value);
        unknownBrokerConfigGroup.add(brokerConfig);
    }

    public void addBrokerConfigGroup(BrokerConfigGroupVO brokerConfigGroupVO) {
        if(brokerConfigGroups == null) {
            brokerConfigGroups = new ArrayList<>();
        }
        brokerConfigGroups.add(brokerConfigGroupVO);
    }
    
    public Collection<BrokerConfigGroupVO> getBrokerConfigGroups() {
        return brokerConfigGroups;
    }

    public void setBrokerConfigGroups(Collection<BrokerConfigGroupVO> brokerConfigGroups) {
        this.brokerConfigGroups = brokerConfigGroups;
    }

    public BrokerConfigGroupVO getUnknownBrokerConfigGroup() {
        return unknownBrokerConfigGroup;
    }

    public void setUnknownBrokerConfigGroup(BrokerConfigGroupVO unknownBrokerConfigGroup) {
        this.unknownBrokerConfigGroup = unknownBrokerConfigGroup;
    }
}
