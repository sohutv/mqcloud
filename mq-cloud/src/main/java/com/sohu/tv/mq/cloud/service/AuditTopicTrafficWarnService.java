package com.sohu.tv.mq.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AuditTopicTrafficWarn;
import com.sohu.tv.mq.cloud.dao.AuditTopicTrafficWarnDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * topic流量预警审核service
 * @author yongweizhao
 * @create 2020/9/24 12:11
 */
@Service
public class AuditTopicTrafficWarnService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditTopicTrafficWarnDao auditTopicTrafficWarnDao;
    
    /**
     * 保存
     */
    public Result<?> save(long aid, long tid, int trafficWarnEnabled) {
        try {
            auditTopicTrafficWarnDao.insert(aid, tid, trafficWarnEnabled);
        } catch (Exception e) {
            logger.error("insert err, aid:{} tid:{}", aid, tid, e);
            throw e;
        }
        return Result.getOKResult();
    }

    /**
     * 按照aid查询
     */
    public Result<AuditTopicTrafficWarn> queryAuditTopicTrafficWarn(long aid) {
        AuditTopicTrafficWarn auditTopicTrafficWarn = null;
        try {
            auditTopicTrafficWarn = auditTopicTrafficWarnDao.selectByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditTopicTrafficWarn err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditTopicTrafficWarn);
    }
}
