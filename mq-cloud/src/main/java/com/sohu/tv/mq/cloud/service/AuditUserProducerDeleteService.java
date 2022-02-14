package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AuditUserProducerDelete;
import com.sohu.tv.mq.cloud.dao.AuditUserProducerDeleteDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * userProducer删除审核dao
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月5日
 */
@Service
public class AuditUserProducerDeleteService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditUserProducerDeleteDao auditUserProducerDeleteDao;
    
    /**
     * 保存
     */
    public void save(long aid, long pid, String producer, String topic, long uid) {
        try {
            auditUserProducerDeleteDao.insert(aid, pid, producer, topic, uid);
        } catch (Exception e) {
            logger.error("insert err {}", topic, e);
            throw e;
        }
    }


    /**
     * 按照aid查询AuditUserProducerDeleteDao
     * 
     * @param Result<AuditUserProducerDelete>
     */
    public Result<AuditUserProducerDelete> queryAuditUserProducerDelete(long aid) {
        AuditUserProducerDelete auditUserProducerDelete = null;
        try {
            auditUserProducerDelete = auditUserProducerDeleteDao.selectByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditUserProducerDelete err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditUserProducerDelete);
    }

    /**
     * 按照uid和producer查询auditUserProducerDeleteDao 用于校验
     * 
     * @param uid
     * @param producer
     * @return
     */
    public Result<List<AuditUserProducerDelete>> queryAuditUserProducerDeleteByUidAndProducer(long uid,
            String producer) {
        List<AuditUserProducerDelete> AuditUserProducerDelete = null;
        try {
            AuditUserProducerDelete = auditUserProducerDeleteDao.selectByUidAndProducer(uid, producer);
        } catch (Exception e) {
            logger.error("queryAuditUserConsumerDelete err, uid:{}, producer:{}", uid, producer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(AuditUserProducerDelete);
    }
}
