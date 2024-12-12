package com.sohu.tv.mq.cloud.service.action;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Status;
import com.sohu.tv.mq.cloud.service.BrokerAutoUpdateStepService;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.MQDeployer;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.sohu.tv.mq.cloud.util.Status.BROKER_AUTO_UPDATE_CHECK_STATUS_ERROR;
import static com.sohu.tv.mq.cloud.util.Status.BROKER_AUTO_UPDATE_CHECK_STATUS_FINISHED;

/**
 * broker行为
 *
 * @author yongfeigao
 * @date 2024年11月06日
 */
public abstract class BrokerAction {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected BrokerAutoUpdateStepService brokerAutoUpdateStepService;

    @Autowired
    protected MQDeployer mqDeployer;

    @Autowired
    protected BrokerService brokerService;

    @Autowired
    protected ClusterService clusterService;

    /**
     * 执行
     */
    public Result<?> execute(BrokerAutoUpdateStep step) {
        // 执行前置操作
        Result<?> beforeResult = beforeExecute(step);
        if (beforeResult.isNotOK()) {
            return beforeResult;
        }
        long start = System.currentTimeMillis();
        // 执行步骤
        Result<?> result = executeStep(step);
        logger.info("executeStep result:{} cost:{}ms, [{}]", result.isOK() ? "ok" : result.getErrorMessage(),
                System.currentTimeMillis() - start, step.toSimpleString());
        // 检查步骤状态
        result = waitAndCheckStepStatus(step, result);
        // 更新数据库状态
        Result<?> afterResult = afterExecute(result, step);
        if (afterResult.isNotOK()) {
            return afterResult;
        }
        return result;
    }

    /**
     * 执行前置操作
     */
    protected Result<?> beforeExecute(BrokerAutoUpdateStep step) {
        // 更新数据库状态
        BrokerAutoUpdateStep brokerAutoUpdateStep = new BrokerAutoUpdateStep();
        brokerAutoUpdateStep.setId(step.getId());
        brokerAutoUpdateStep.setStatus(Status.RUNNING.getValue());
        brokerAutoUpdateStep.setStartTime(new Date());
        return brokerAutoUpdateStepService.update(brokerAutoUpdateStep);
    }

    /**
     * 执行后置操作
     */
    protected Result<?> afterExecute(Result<?> result, BrokerAutoUpdateStep step) {
        // 如果已经结束，直接返回
        if (BROKER_AUTO_UPDATE_CHECK_STATUS_FINISHED.getKey() == result.getStatus()) {
            return result;
        }
        // 更新数据库状态
        BrokerAutoUpdateStep brokerAutoUpdateStep = new BrokerAutoUpdateStep();
        brokerAutoUpdateStep.setId(step.getId());
        brokerAutoUpdateStep.setEndTime(new Date());
        if (result.isOK()) {
            brokerAutoUpdateStep.setStatus(Status.SUCCESS.getValue());
            brokerAutoUpdateStep.setInfo("");
        } else {
            brokerAutoUpdateStep.setStatus(Status.FAILED.getValue());
            brokerAutoUpdateStep.setInfo(result.getErrorMessage());
        }
        return brokerAutoUpdateStepService.update(brokerAutoUpdateStep);
    }

    /**
     * 执行
     */
    protected abstract Result<?> executeStep(BrokerAutoUpdateStep step);

    /**
     * 等待检查步骤状态
     */
    protected Result<?> waitAndCheckStepStatus(BrokerAutoUpdateStep step, Result<?> executeResult) {
        if (executeResult.isNotOK()) {
            return executeResult;
        }
        int okTimes = 0;
        for (int i = 1; i <= stepCheckStatusMaxCheckTimes(); ++i) {
            boolean okTimesReset = false;
            Result<?> checkResult = stepCheckStatusOK(step, executeResult);
            // 检查状态异常，直接返回
            if (BROKER_AUTO_UPDATE_CHECK_STATUS_ERROR.getKey() == checkResult.getStatus()) {
                logger.error("cannot check status:{}, step:{}", checkResult.getErrorMessage(), step);
                return checkResult;
            }
            if (checkResult.isOK()) {
                if (++okTimes >= stepCheckStatusContinuousOKTimes()) {
                    // 检查状态OK
                    if (okTimes > 1) {
                        logger.info("check ok, times:{}, okTimes:{}={}, [{}]", i, okTimes,
                                stepCheckStatusContinuousOKTimes(), step.toSimpleString());
                    }
                    return Result.getOKResult();
                }
            } else {
                if (okTimes != 0) {
                    okTimesReset = true;
                    okTimes = 0;
                }
            }
            // 手动结束后，直接返回
            BrokerAutoUpdateStep tempStep = brokerAutoUpdateStepService.selectById(step.getId()).getResult();
            if (tempStep != null && tempStep.isFinished()) {
                return Result.getResult(BROKER_AUTO_UPDATE_CHECK_STATUS_FINISHED);
            }
            // 保存检查状态信息
            logAndSaveCheckStatusInfo(checkResult, i, okTimes, step, okTimesReset);
            try {
                TimeUnit.SECONDS.sleep(stepCheckStatusWaitSeconds());
            } catch (InterruptedException e) {
                logger.error("sleep interrupted", e);
            }
        }
        saveCheckStatusInfo(step, "check status failed, times:" + stepCheckStatusContinuousOKTimes());
        return Result.getErrorResult("check status times:" + stepCheckStatusMaxCheckTimes() + " failed");
    }

    /**
     * 记录日志并保存检查状态信息
     */
    private void logAndSaveCheckStatusInfo(Result<?> checkResult, int i, int okTimes, BrokerAutoUpdateStep step, boolean okTimesReset) {
        String okTimeResetFlag = "";
        if (okTimesReset) {
            okTimeResetFlag = "[okTimeReset]";
        }
        logger.info("check result" + okTimeResetFlag + ":{}, times:{}, okTimes:{}<{}, [{}]",
                checkResult.getErrorMessage(), i, okTimes, stepCheckStatusContinuousOKTimes(), step.toSimpleString());
        saveCheckStatusInfo(step, "check result" + okTimeResetFlag + ":" + checkResult.getErrorMessage() + ", times:" + i
                + ", okTimes:" + okTimes + "<" + stepCheckStatusContinuousOKTimes());
    }

    /**
     * 保存检查状态信息
     */
    private void saveCheckStatusInfo(BrokerAutoUpdateStep step, String info) {
        BrokerAutoUpdateStep brokerAutoUpdateStep = new BrokerAutoUpdateStep();
        brokerAutoUpdateStep.setId(step.getId());
        brokerAutoUpdateStep.setInfo(info);
        brokerAutoUpdateStepService.update(brokerAutoUpdateStep);
    }

    /**
     * 步骤检查状态是否OK
     */
    protected Result<?> stepCheckStatusOK(BrokerAutoUpdateStep step, Result<?> executeResult) {
        return Result.getOKResult();
    }

    /**
     * 步骤检查状态时间等待
     */
    protected int stepCheckStatusWaitSeconds() {
        return 35;
    }

    /**
     * 步骤检查状态连续OK次数
     */
    protected int stepCheckStatusContinuousOKTimes() {
        return 1;
    }

    /**
     * 步骤检查状态最大检测次数
     * @return
     */
    protected int stepCheckStatusMaxCheckTimes() {
        return 100;
    }

    /**
     * 获取行为
     */
    protected abstract Action getAction();
}
