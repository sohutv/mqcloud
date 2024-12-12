package com.sohu.tv.mq.cloud.service.action;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

/**
 * broker关闭行为
 *
 * @author yongfeigao
 * @date 2024年11月06日
 */
@Component
public class BrokerShutdownAction extends BrokerAction {

    @Override
    protected Result<?> executeStep(BrokerAutoUpdateStep step) {
        return mqDeployer.shutdown(step.getIp(), step.getPort());
    }

    @Override
    protected Result<?> stepCheckStatusOK(BrokerAutoUpdateStep step, Result<?> executeResult) {
        String pidString = String.valueOf(executeResult.getResult());
        int pid = NumberUtils.toInt(pidString, 0);
        if (pid == 0) {
            return Result.getResult(Status.BROKER_AUTO_UPDATE_CHECK_STATUS_ERROR).setMessage("pid is" + pidString);
        }
        return mqDeployer.isPidDead(step.getIp(), pid);
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
        return Action.SHUTDOWN;
    }
}
