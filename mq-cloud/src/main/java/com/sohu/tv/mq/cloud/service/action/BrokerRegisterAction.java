package com.sohu.tv.mq.cloud.service.action;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.BrokerConfigUpdateParam;
import org.springframework.stereotype.Component;

/**
 * broker注册行为
 *
 * @author yongfeigao
 * @date 2024年11月06日
 */
@Component
public class BrokerRegisterAction extends BrokerAction {

    @Override
    protected Result<?> executeStep(BrokerAutoUpdateStep step) {
        BrokerConfigUpdateParam brokerConfigUpdateParam = new BrokerConfigUpdateParam();
        brokerConfigUpdateParam.setCid(step.getCid());
        brokerConfigUpdateParam.setAddr(step.getBrokerAddr());
        brokerConfigUpdateParam.setConfig("registerBroker:true;");
        return brokerService.updateBrokerConfig(brokerConfigUpdateParam);
    }

    @Override
    protected Action getAction() {
        return Action.REGISTER;
    }
}
