package com.sohu.tv.mq.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AuditConsumerDelete;
import com.sohu.tv.mq.cloud.dao.AuditConsumerDeleteDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * consumer删除审核dao
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
@Service
public class AuditConsumerDeleteService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private AuditConsumerDeleteDao auditConsumerDeleteDao;
    
    /**
     * 保存
     */
    public void save(long aid, long cid, String consumer, String topic) {
        try {
            auditConsumerDeleteDao.insert(aid, cid, consumer, topic);
        } catch (Exception e) {
            logger.error("insert err {}", topic, e);
            throw e;
        }
    }
    
    /**
     * 按照aid查询AuditConsumerDeleteDao
     * 
     * @param Result<AuditConsumerDelete>
     */
    public Result<AuditConsumerDelete> queryAuditConsumerDelete(long aid) {
        AuditConsumerDelete auditConsumerDelete = null;
        try {
            auditConsumerDelete = auditConsumerDeleteDao.selectByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditConsumerDelete err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditConsumerDelete);
    }
}
