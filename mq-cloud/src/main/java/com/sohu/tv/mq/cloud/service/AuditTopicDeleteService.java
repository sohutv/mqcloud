package com.sohu.tv.mq.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AuditTopicDelete;
import com.sohu.tv.mq.cloud.dao.AuditTopicDeleteDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * topic删除审核dao
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
@Service
public class AuditTopicDeleteService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private AuditTopicDeleteDao auditTopicDeleteDao;
    
    /**
     * 保存
     */
    public void save(long aid, long tid, String topic) {
        try {
            auditTopicDeleteDao.insert(aid, tid, topic);
        } catch (Exception e) {
            logger.error("insert err {}", topic, e);
            throw e;
        }
    }
    
    /**
     * 按照aid查询AuditTopicDelete
     * 
     * @param Result<AuditTopicDelete>
     */
    public Result<AuditTopicDelete> queryAuditTopicDelete(long aid) {
        AuditTopicDelete auditTopicDelete = null;
        try {
            auditTopicDelete = auditTopicDeleteDao.selectByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditTopicDelete err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditTopicDelete);
    }
}
