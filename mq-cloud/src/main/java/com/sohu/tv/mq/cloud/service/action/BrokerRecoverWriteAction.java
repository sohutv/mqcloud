package com.sohu.tv.mq.cloud.service.action;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;
import com.sohu.tv.mq.cloud.common.model.ClientConnectionSize;
import com.sohu.tv.mq.cloud.util.Result;
import org.springframework.stereotype.Component;

/**
 * broker恢复写入行为
 *
 * @author yongfeigao
 * @date 2024年11月06日
 */
@Component
public class BrokerRecoverWriteAction extends BrokerAction {

    @Override
    protected Result<?> executeStep(BrokerAutoUpdateStep step) {
        return brokerService.addWritePerm(step.toBroker());
    }

    @Override
    protected Result<?> stepCheckStatusOK(BrokerAutoUpdateStep step, Result<?> executeResult) {
        // 检查broker链接数
        Result<ClientConnectionSize> result = brokerService.getClientConnectionSize(step.getCid(), step.getBrokerAddr());
        if (result.isNotOK()) {
            return result;
        }
        if (result.getResult().getProducerConnectionSize() > 0) {
            return Result.getOKResult().setMessage("producer connection size:" + result.getResult().getProducerConnectionSize());
        }
        return Result.getErrorResult("producer connection size is 0");
    }

    @Override
    protected int stepCheckStatusContinuousOKTimes() {
        return 3;
    }

    @Override
    protected Action getAction() {
        return Action.RECOVER_WRITE;
    }
}
