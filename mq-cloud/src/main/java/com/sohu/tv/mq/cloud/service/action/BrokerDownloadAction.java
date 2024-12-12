package com.sohu.tv.mq.cloud.service.action;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;
import com.sohu.tv.mq.cloud.util.Result;
import org.springframework.stereotype.Component;

/**
 * broker下载行为
 *
 * @author yongfeigao
 * @date 2024年11月06日
 */
@Component
public class BrokerDownloadAction extends BrokerAction {

    @Override
    protected Result<?> executeStep(BrokerAutoUpdateStep step) {
        return mqDeployer.scp(step.getIp(), step.getRocketMQVersion());
    }

    @Override
    protected Action getAction() {
        return Action.DOWNLOAD;
    }
}
