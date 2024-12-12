package com.sohu.tv.mq.cloud.service.action;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;
import com.sohu.tv.mq.cloud.util.Result;
import org.springframework.stereotype.Component;

/**
 * broker恢复数据行为
 *
 * @author yongfeigao
 * @date 2024年11月06日
 */
@Component
public class BrokerRecoverDataAction extends BrokerAction {

    @Override
    protected Result<?> executeStep(BrokerAutoUpdateStep step) {
        return mqDeployer.recover(step.getIp(), step.getBrokerBaseDir());
    }

    @Override
    protected Action getAction() {
        return Action.RECOVER_DATA;
    }
}
