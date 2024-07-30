package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.AuditHttpConsumerConfig;
import com.sohu.tv.mq.cloud.dao.AuditHttpConsumerConfigDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * http消费者配置审核服务
 *
 * @author yongfeigao
 * @date 2024年7月12日
 */
@Service
public class AuditHttpConsumerConfigService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditHttpConsumerConfigDao auditHttpConsumerConfigDao;

    /**
     * 保存
     */
    public Result<?> save(AuditHttpConsumerConfig auditHttpConsumerConfig) {
        try {
            auditHttpConsumerConfigDao.insert(auditHttpConsumerConfig);
        } catch (Exception e) {
            logger.error("insert err {}", auditHttpConsumerConfig, e);
            throw e;
        }
        return Result.getOKResult();
    }

    /**
     * 按照aid查询
     */
    public Result<AuditHttpConsumerConfig> query(long aid) {
        try {
            return Result.getResult(auditHttpConsumerConfigDao.select(aid));
        } catch (Exception e) {
            logger.error("query err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询未审核的数量
     *
     * @param consumerId
     * @return
     */
    public Result<Integer> queryUnAuditCount(long consumerId) {
        try {
            return Result.getResult(auditHttpConsumerConfigDao.selectUnAuditCount(consumerId));
        } catch (Exception e) {
            logger.error("query err, consumerId:{}", consumerId, e);
            return Result.getDBErrorResult(e);
        }
    }
}
