package com.sohu.tv.mq.cloud.service.action;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.protocol.body.BrokerStatsData;
import org.springframework.stereotype.Component;

/**
 * broker停写行为
 *
 * @author yongfeigao
 * @date 2024年11月06日
 */
@Component
public class BrokerStopWriteAction extends BrokerAction {

    @Override
    public Result<?> executeStep(BrokerAutoUpdateStep step) {
        return brokerService.wipeWritePerm(step.getCid(), step.getBrokerName(), step.getBrokerAddr());
    }

    @Override
    protected Result<?> stepCheckStatusOK(BrokerAutoUpdateStep step, Result<?> executeResult) {
        // 检查broker是否停写
        Result<BrokerStatsData> result = brokerService.viewBrokerPutStats(step.getCid(), step.getBrokerAddr());
        if (result.isNotOK()) {
            // 如果查询失败，且异常是MQClientException，且异常信息包含"not exist"，说明broker上没有put stats数据，说明broker停写了
            if (result.getException() != null && result.getException() instanceof MQClientException) {
                String error = ((MQClientException) result.getException()).getErrorMessage();
                if (error != null && error.contains("not exist")) {
                    return Result.getOKResult().setMessage("put stats:0");
                }
            }
            return result;
        }
        long putStats = result.getResult().getStatsMinute().getSum();
        if (putStats <= 0) {
            return Result.getOKResult().setMessage("put stats:0");
        }
        return Result.getErrorResult("put stats:" + putStats);
    }

    @Override
    protected int stepCheckStatusContinuousOKTimes() {
        return 2;
    }

    @Override
    protected int stepCheckStatusMaxCheckTimes() {
        return 15;
    }

    @Override
    protected Action getAction() {
        return Action.STOP_WRITE;
    }
}
