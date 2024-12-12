package com.sohu.tv.mq.cloud.service.action;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * broker行为选择器
 *
 * @author yongfeigao
 * @date 2024年11月06日
 */
@Component
public class BrokerActionChooser {

    @Autowired
    private List<BrokerAction> brokerActionList;

    private Map<Action, BrokerAction> actionMap;

    @PostConstruct
    public void init() {
        actionMap = brokerActionList.stream().collect(Collectors.toMap(BrokerAction::getAction, brokerAction -> brokerAction));
    }

    /**
     * 选择步骤Action
     */
    public BrokerAction choose(Action action) {
        return actionMap.get(action);
    }
}
