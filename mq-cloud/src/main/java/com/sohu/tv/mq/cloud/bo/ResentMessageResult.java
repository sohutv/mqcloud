package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.common.protocol.body.CMResult;
import org.apache.rocketmq.common.protocol.body.ConsumeMessageDirectlyResult;

/**
 * 重发消息结果
 * 
 * @author yongfeigao
 * @date 2020年10月22日
 */
public class ResentMessageResult {
    private String clientId;
    private ConsumeMessageDirectlyResult consumeMessageDirectlyResult;

    public ResentMessageResult(String clientId, ConsumeMessageDirectlyResult consumeMessageDirectlyResult) {
        this.clientId = clientId;
        this.consumeMessageDirectlyResult = consumeMessageDirectlyResult;
    }

    public boolean isOK() {
        if (consumeMessageDirectlyResult == null) {
            return false;
        }
        if (CMResult.CR_SUCCESS != consumeMessageDirectlyResult.getConsumeResult()) {
            return false;
        }
        return true;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public ConsumeMessageDirectlyResult getConsumeMessageDirectlyResult() {
        return consumeMessageDirectlyResult;
    }

    public void setConsumeMessageDirectlyResult(ConsumeMessageDirectlyResult consumeMessageDirectlyResult) {
        this.consumeMessageDirectlyResult = consumeMessageDirectlyResult;
    }

    @Override
    public String toString() {
        return "ResentMessageResult [clientId=" + clientId + ", consumeMessageDirectlyResult="
                + consumeMessageDirectlyResult + "]";
    }
}
