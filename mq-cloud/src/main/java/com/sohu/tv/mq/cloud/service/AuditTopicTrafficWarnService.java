package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.AuditTopicTrafficWarn;
import com.sohu.tv.mq.cloud.dao.AuditTopicTrafficWarnDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
