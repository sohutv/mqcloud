package com.sohu.tv.mq.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AuditConsumerConfig;
import com.sohu.tv.mq.cloud.dao.AuditConsumerConfigDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 消费者配置审核服务
 * 
 * @author yongfeigao
 * @date 2020年6月4日
 */
@Service
public class AuditConsumerConfigService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditConsumerConfigDao auditConsumerConfigDao;

    /**
     * 保存
     */
    public Result<?> save(AuditConsumerConfig auditConsumerConfig) {
        try {
            auditConsumerConfigDao.insert(auditConsumerConfig);
        } catch (Exception e) {
            logger.error("insert err {}", auditConsumerConfig, e);
            throw e;
        }
        return Result.getOKResult();
    }
    
    /**
     * 按照aid查询
     * 
     * @param Result<AuditConsumerConfig>
     */
    public Result<AuditConsumerConfig> query(long aid) {
        AuditConsumerConfig auditConsumerConfig = null;
        try {
            auditConsumerConfig = auditConsumerConfigDao.select(aid);
        } catch (Exception e) {
            logger.error("query err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditConsumerConfig);
    }
}
