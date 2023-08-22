package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.CancelUniqId;
import com.sohu.tv.mq.cloud.dao.CancelUniqIdDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description 取消定时消息ID记录表Service
 * @date 2023/7/28 16:47:06
 */
@Service
public class CancelUniqIdService {

    @Resource
    private CancelUniqIdDao cancelUniqIdDao;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 保存单条记录
     */
    public int save(Long tid, String uniqId) {
        try {
            return cancelUniqIdDao.save(tid, uniqId, new Date());
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate uniqId:{}", uniqId);
            throw e;
        } catch (Exception e) {
            logger.error("insert err, uniqId:{}", uniqId, e);
            throw e;
        }
    }

    /**
     * 按照uniqueIds批量查询
     */
    public Result<List<CancelUniqId>> queryByUniqIds(List<String> uniqIds, Long tid) {
        List<CancelUniqId> cancelUniqIds = null;
        if (CollectionUtils.isEmpty(uniqIds) || tid == null) {
            return Result.getRequestErrorResult(new IllegalArgumentException("uniqIds or tid is empty"));
        }
        try {
            cancelUniqIds = cancelUniqIdDao.queryByUniqIds(tid, uniqIds);
        } catch (Exception e) {
            logger.error("queryByUniqIds err, uniqIds:{}", uniqIds, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(cancelUniqIds);
    }

    /**
     * 按照uniqueId查询单个
     */
    public Result<CancelUniqId> queryOneByUniqId(String uniqId) {
        if (StringUtils.isBlank(uniqId)) {
            return Result.getRequestErrorResult(new IllegalArgumentException("uniqIds or tid is empty"));
        }
        try {
            CancelUniqId cancelUniqId = cancelUniqIdDao.queryOneByUniqId(uniqId);
            return Result.getResult(cancelUniqId);
        } catch (Exception e) {
            logger.error("queryOneByUniqId err, uniqIds:{}", uniqId, e);
            return Result.getDBErrorResult(e);
        }
    }
}
