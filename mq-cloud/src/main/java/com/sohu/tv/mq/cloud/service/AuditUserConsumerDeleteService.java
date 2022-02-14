package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AuditUserConsumerDelete;
import com.sohu.tv.mq.cloud.dao.AuditUserConsumerDeleteDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * UserConsumer删除审核service
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月7日
 */
@Service
public class AuditUserConsumerDeleteService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditUserConsumerDeleteDao auditUserConsumerDeleteDao;

    /**
     * 保存
     */
    public Result<?> save(long aid, long ucid, String consumer, String topic, long uid) {
        try {
            auditUserConsumerDeleteDao.insert(aid, ucid, consumer, topic, uid);
        } catch (Exception e) {
            logger.error("insert err, aid:{} consumer:{}", aid, consumer, e);
            throw e;
        }
        return Result.getOKResult();
    }
    
    /**
     * 按照aid查询AuditUserConsumerDeleteDao
     * 
     * @param Result<AuditUserConsumerDelete>
     */
    public Result<AuditUserConsumerDelete> queryAuditUserConsumerDelete(long aid) {
        AuditUserConsumerDelete auditUserConsumerDelete = null;
        try {
            auditUserConsumerDelete = auditUserConsumerDeleteDao.selectByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditUserConsumerDelete err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditUserConsumerDelete);
    }
    
    /**
     * 按照uid和consumer查询AuditUserConsumerDeleteDao
     * 用于校验
     * @param uid
     * @param consumer
     * @return
     */
    public Result<List<AuditUserConsumerDelete>> queryAuditUserConsumerDeleteByUidAndConsumer(long uid, String consumer) {
        List<AuditUserConsumerDelete> auditUserConsumerDelete = null;
        try {
            auditUserConsumerDelete = auditUserConsumerDeleteDao.selectByUidAndConsumer(uid, consumer);
        } catch (Exception e) {
            logger.error("queryAuditUserConsumerDelete err, uid:{}, consumer:{}", uid, consumer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditUserConsumerDelete);
    }
}
