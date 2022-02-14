package com.sohu.tv.mq.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AuditTimespanMessageConsume;
import com.sohu.tv.mq.cloud.dao.AuditTimespanMessageConsumeDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 时间段消息消费审核服务
 * 
 * @author yongfeigao
 * @date 2021年11月24日
 */
@Service
public class AuditTimespanMessageConsumeService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditTimespanMessageConsumeDao aditTimespanMessageConsumeDao;

    /**
     * 保存
     * 
     * @return
     */
    public Result<?> save(AuditTimespanMessageConsume auditTimespanMessageConsume) {
        try {
            aditTimespanMessageConsumeDao.insert(auditTimespanMessageConsume);
        } catch (Exception e) {
            logger.error("insert err:{}", auditTimespanMessageConsume, e);
            throw e;
        }
        return Result.getOKResult();
    }

    /**
     * 查询
     * 
     * @param id
     * @return
     */
    public Result<AuditTimespanMessageConsume> query(long id) {
        try {
            return Result.getResult(aditTimespanMessageConsumeDao.selectByAid(id));
        } catch (Exception e) {
            logger.error("query err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
    }
}