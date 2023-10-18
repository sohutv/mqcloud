package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.AuditTimespanMessageExport;
import com.sohu.tv.mq.cloud.dao.AuditTimespanMessageExportDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 时间段消息导出审核服务
 *
 * @author yongfeigao
 * @date 2023/9/21
 */
@Service
public class AuditTimespanMessageExportService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditTimespanMessageExportDao auditTimespanMessageExportDao;

    /**
     * 保存
     *
     * @return
     */
    public Result<?> save(AuditTimespanMessageExport auditTimespanMessageExport) {
        try {
            auditTimespanMessageExportDao.insert(auditTimespanMessageExport);
        } catch (Exception e) {
            logger.error("insert err:{}", auditTimespanMessageExport, e);
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
    public Result<AuditTimespanMessageExport> query(long id) {
        try {
            return Result.getResult(auditTimespanMessageExportDao.selectByAid(id));
        } catch (Exception e) {
            logger.error("query err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
    }
}