package com.sohu.tv.mq.cloud.service.action;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;
import com.sohu.tv.mq.cloud.util.Result;
import org.springframework.stereotype.Component;

/**
 * broker启动行为
 *
 * @author yongfeigao
 * @date 2024年11月06日
 */
@Component
public class BrokerStartAction extends BrokerAction {

    @Override
    protected Result<?> executeStep(BrokerAutoUpdateStep step) {
        return mqDeployer.startup(step.getIp(), step.getBrokerBaseDir(), step.getPort(), false);
    }

    @Override
    protected Result<?> stepCheckStatusOK(BrokerAutoUpdateStep step, Result<?> executeResult) {
        Result<?> result = mqDeployer.getProgram(step.getIp(), step.getPort());
        if (result.isNotOK()) {
            return result;
        }
        if (result.getResult() == null) {
            return Result.getErrorResult("no broker process");
        }
        return Result.getOKResult().setMessage("broker is running");
    }

    @Override
    protected int stepCheckStatusWaitSeconds() {
        return 10;
    }

    @Override
    protected int stepCheckStatusContinuousOKTimes() {
        return 2;
    }

    @Override
    protected Action getAction() {
        return Action.START;
    }
}
