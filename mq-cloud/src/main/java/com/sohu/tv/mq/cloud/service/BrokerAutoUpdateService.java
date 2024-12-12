package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdate;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.dao.BrokerAutoUpdateDao;
import com.sohu.tv.mq.cloud.dao.BrokerAutoUpdateStepDao;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

/**
 * broker自动更新服务
 *
 * @author yongfeigao
 * @date 2024年10月31日
 */
@Service
public class BrokerAutoUpdateService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BrokerAutoUpdateDao brokerAutoUpdateDao;

    @Autowired
    private BrokerAutoUpdateStepDao brokerAutoUpdateStepDao;

    /**
     * 保存
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<?> save(int cid, List<BrokerAutoUpdateStep> steps) {
        try {
            // 保存BrokerAutoUpdate
            BrokerAutoUpdate brokerAutoUpdate = new BrokerAutoUpdate();
            brokerAutoUpdate.setStatus(BrokerAutoUpdate.Status.INIT.getValue());
            brokerAutoUpdate.setCid(cid);
            brokerAutoUpdateDao.insert(brokerAutoUpdate);
            // 保存BrokerAutoUpdateStep
            steps.forEach(step -> step.setBrokerAutoUpdateId(brokerAutoUpdate.getId()));
            brokerAutoUpdateStepDao.batchInsert(steps);
            return Result.getResult(Status.OK);
        } catch (Exception e) {
            logger.error("save:{}", cid, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 更新
     */
    public Result<Integer> update(BrokerAutoUpdate brokerAutoUpdate) {
        try {
            return Result.getResult(brokerAutoUpdateDao.update(brokerAutoUpdate));
        } catch (Exception e) {
            logger.error("update:{}", brokerAutoUpdate, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 根据cid查询
     */
    public Result<List<BrokerAutoUpdate>> selectByCid(int cid) {
        try {
            return Result.getResult(brokerAutoUpdateDao.selectByCid(cid));
        } catch (Exception e) {
            logger.error("selectByCid:{}", cid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 根据cid查询未完成的
     */
    public Result<List<BrokerAutoUpdate>> selectUndoneByCid(int cid) {
        try {
            return Result.getResult(brokerAutoUpdateDao.selectUndoneByCid(cid, BrokerAutoUpdate.Status.UNDONE_STATUS));
        } catch (Exception e) {
            logger.error("selectUndoneByCid:{}", cid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询可执行状态的
     */
    public Result<List<BrokerAutoUpdate>> selectExecutable() {
        try {
            return Result.getResult(brokerAutoUpdateDao.selectExecutable(BrokerAutoUpdate.Status.EXECUTE_STATUS));
        } catch (Exception e) {
            logger.error("selectExecutable", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 根据id查询
     */
    public Result<BrokerAutoUpdate> selectById(int id) {
        try {
            return Result.getResult(brokerAutoUpdateDao.selectById(id));
        } catch (Exception e) {
            logger.error("selectById:{}", id, e);
            return Result.getDBErrorResult(e);
        }
    }
}
