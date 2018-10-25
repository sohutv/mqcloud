package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohu.tv.mq.cloud.bo.AuditAssociateConsumer;
import com.sohu.tv.mq.cloud.dao.AuditAssociateConsumerDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 审核关联消费者
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月17日
 */
@Service
public class AssociateConsumerService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private AuditAssociateConsumerDao auditAssociateConsumerDao;
    
    /**
     * 保存AuditAssociateConsumer记录
     * 
     * @return 返回Result
     */
    @Transactional
    public Result<?> save(AuditAssociateConsumer auditAssociateConsumer) {
        try {
            auditAssociateConsumerDao.insert(auditAssociateConsumer);
        } catch (Exception e) {
            logger.error("insert err, auditAssociateConsumer:{}", auditAssociateConsumer, e);
            throw e;
        }
        return Result.getOKResult();
    }
    
    /**
     * 查询AuditAssociateConsumer记录
     * 
     * @return 返回Result<AuditAssociateConsumer>
     */
    public Result<AuditAssociateConsumer> query(long aid) {
        AuditAssociateConsumer auditAssociateConsumer = null;
        try {
            auditAssociateConsumer = auditAssociateConsumerDao.select(aid);
        } catch (Exception e) {
            logger.error("query err, auditAssociateConsumer:{}", aid, e);
        }
        return Result.getResult(auditAssociateConsumer);
    }

    /**
     * 查询AuditAssociateConsumer记录
     * 
     * @return 返回Result<AuditAssociateConsumer>
     */
    public Result<List<AuditAssociateConsumer>> queryByUidAndCid(long uid, long cid) {
        List<AuditAssociateConsumer> auditAssociateConsumer = null;
        try {
            auditAssociateConsumer = auditAssociateConsumerDao.selectByUidAndCid(uid, cid);
        } catch (Exception e) {
            logger.error("selectByUidAndCid err, uid:{}, cid:{}", uid, cid, e);
        }
        return Result.getResult(auditAssociateConsumer);
    }
}
