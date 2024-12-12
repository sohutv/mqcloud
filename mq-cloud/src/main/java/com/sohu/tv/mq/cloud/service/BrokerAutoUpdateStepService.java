package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Status;
import com.sohu.tv.mq.cloud.dao.BrokerAutoUpdateStepDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * broker自动更新步骤服务
 *
 * @author yongfeigao
 * @date 2024年11月04日
 */
@Service
public class BrokerAutoUpdateStepService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BrokerAutoUpdateStepDao brokerAutoUpdateStepDao;

    /**
     * 更新
     */
    public Result<Integer> update(BrokerAutoUpdateStep brokerAutoUpdateStep) {
        try {
            return Result.getResult(brokerAutoUpdateStepDao.update(brokerAutoUpdateStep));
        } catch (Exception e) {
            logger.error("update:{}", brokerAutoUpdateStep, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 根据brokerAutoUpdateId查询
     */
    public Result<List<BrokerAutoUpdateStep>> selectByBrokerAutoUpdateId(int brokerAutoUpdateId) {
        try {
            return Result.getResult(brokerAutoUpdateStepDao.selectByBrokerAutoUpdateId(brokerAutoUpdateId));
        } catch (Exception e) {
            logger.error("selectByBrokerAutoUpdateId:{}", brokerAutoUpdateId, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 根据brokerAutoUpdateId查询可以执行的
     */
    public Result<BrokerAutoUpdateStep> selectExecutable(int brokerAutoUpdateId) {
        try {
            return Result.getResult(brokerAutoUpdateStepDao.selectExecutable(brokerAutoUpdateId, Status.EXECUTE_STATUS));
        } catch (Exception e) {
            logger.error("selectByBrokerAutoUpdateId:{}", brokerAutoUpdateId, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 根据id查询
     */
    public Result<BrokerAutoUpdateStep> selectById(int id) {
        try {
            return Result.getResult(brokerAutoUpdateStepDao.selectById(id));
        } catch (Exception e) {
            logger.error("selectById:{}", id, e);
            return Result.getDBErrorResult(e);
        }
    }
}
