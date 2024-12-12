package com.sohu.tv.mq.cloud.service.action;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;
import com.sohu.tv.mq.cloud.common.model.ClientConnectionSize;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.BrokerConfigUpdateParam;
import org.springframework.stereotype.Component;

/**
 * broker取消注册行为
 *
 * @author yongfeigao
 * @date 2024年11月06日
 */
@Component
public class BrokerUnregisterAction extends BrokerAction {

    @Override
    protected Result<?> executeStep(BrokerAutoUpdateStep step) {
        BrokerConfigUpdateParam brokerConfigUpdateParam = new BrokerConfigUpdateParam();
        brokerConfigUpdateParam.setCid(step.getCid());
        brokerConfigUpdateParam.setAddr(step.getBrokerAddr());
        brokerConfigUpdateParam.setConfig("registerBroker:false;");
        return brokerService.updateBrokerConfig(brokerConfigUpdateParam);
    }

    @Override
    protected Result<?> stepCheckStatusOK(BrokerAutoUpdateStep step, Result<?> executeResult) {
        // 检查broker链接数
        Result<ClientConnectionSize> result = brokerService.getClientConnectionSize(step.getCid(), step.getBrokerAddr());
        if (result.isNotOK()) {
            return result;
        }
        int producerConnectionSize = result.getResult().getProducerConnectionSize();
        if(producerConnectionSize > 0) {
            return Result.getErrorResult("producer connection size:" + producerConnectionSize);
        }
        int consumerConnectionSize = result.getResult().getConsumerConnectionSize();
        if(consumerConnectionSize > 0) {
            return Result.getErrorResult("consumer connection size:" + consumerConnectionSize);
        }
        return Result.getOKResult().setMessage("no connection");
    }

    @Override
    protected int stepCheckStatusContinuousOKTimes() {
        return 2;
    }

    @Override
    protected Action getAction() {
        return Action.UNREGISTER;
    }
}
