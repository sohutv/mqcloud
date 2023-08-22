package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.AuditWheelMessageCancel;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.dao.AuditWheelMessageCancelDao;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.AuditWheelCancelCheckVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description 定时消息取消Service
 * @date 2023/7/5 18:25:42
 */
@Service
public class AuditWheelMessageCancelService {

    @Resource
    private AuditWheelMessageCancelDao auditWheelMessageCancelDao;

    @Autowired
    private TopicService topicService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 保存单条记录
     */
    public Result<?> save(AuditWheelMessageCancel auditWheelMessageCancel) {
        try {
            auditWheelMessageCancelDao.insert(auditWheelMessageCancel);
        } catch (Exception e) {
            logger.error("insert err, auditWheelMessageCancel:{}", auditWheelMessageCancel, e);
            throw e;
        }
        return Result.getOKResult();
    }

    /**
     * 批量保存记录
     */
    public Result<?> saveBatch(List<AuditWheelMessageCancel> auditWheelMessageCancels) {
        try {
            auditWheelMessageCancelDao.insertBatch(auditWheelMessageCancels);
        } catch (Exception e) {
            logger.error("insert err, auditWheelMessageCancel:{}", auditWheelMessageCancels, e);
            throw e;
        }
        return Result.getOKResult();
    }

    /**
     * 按照aid查询AuditWheelMessageCancel
     *
     * @param aid
     * @return Result<List<AuditWheelMessageCancel>>
     */
    public Result<List<AuditWheelMessageCancel>> queryByAid(long aid) {
        List<AuditWheelMessageCancel> auditWheelMessageCancel = null;
        try {
            auditWheelMessageCancel = auditWheelMessageCancelDao.selectByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditWheelMessageCancel err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditWheelMessageCancel);
    }

    /**
     * 按照aid查询未成功执行的AuditWheelMessageCancel
     *
     * @param aid
     * @return Result<List<AuditWheelMessageCancel>>
     */
    public Result<List<AuditWheelMessageCancel>> queryNotCancelAuditByAid(long aid) {
        List<AuditWheelMessageCancel> auditWheelMessageCancel = null;
        try {
            auditWheelMessageCancel = auditWheelMessageCancelDao.selectNotCancelAuditByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditWheelMessageCancel err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditWheelMessageCancel);
    }

    /**
     * 按照uniqId查询AuditWheelMessageCancel
     *
     * @param uniqId
     * @return Result<AuditWheelMessageCancel>
     */
    public Result<AuditWheelMessageCancel> queryByUniqId(String uniqId) {
        AuditWheelMessageCancel auditWheelMessageCancel = null;
        try {
            auditWheelMessageCancel = auditWheelMessageCancelDao.selectByUniqueId(uniqId);
        } catch (Exception e) {
            logger.error("queryAuditWheelMessageCancel err, uniqId:{}", uniqId, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditWheelMessageCancel);
    }

    /**
     * 校验取消消息申请状态
     *
     * @param aid
     * @return Result<?>
     */
    public Result<?> checkCancel(long aid) {
        try {
            Result<List<AuditWheelMessageCancel>> applyCancelList = queryByAid(aid);
            if (applyCancelList.isNotOK()) {
                return applyCancelList;
            }
            List<AuditWheelMessageCancel> auditWheelMessageCancels = applyCancelList.getResult();
            if (auditWheelMessageCancels == null || auditWheelMessageCancels.isEmpty()) {
                return Result.getResult(Status.NO_RESULT);
            }
            Result<Topic> topicResult = topicService.queryTopic(auditWheelMessageCancels.get(0).getTid());
            if (topicResult.isNotOK()) {
                return topicResult;
            }
            AuditWheelCancelCheckVo cancelCheckVo = new AuditWheelCancelCheckVo();
            cancelCheckVo.setValidCancelNum(auditWheelMessageCancels.size());
            cancelCheckVo.setTopicName(topicResult.getResult().getName());

            Result<List<AuditWheelMessageCancel>> notCancelList = queryNotCancelAuditByAid(aid);
            if (notCancelList.isNotOK()) {
                return notCancelList;
            }
            List<String> notCancelIds = Optional.ofNullable(notCancelList.getResult()).
                    map(list -> list.stream().map(AuditWheelMessageCancel::getUniqueId).collect(Collectors.toList())).
                    orElse(Collections.emptyList());
            int index = 1;
            for (AuditWheelMessageCancel auditWheelMessageCancel : auditWheelMessageCancels) {
                AuditWheelCancelCheckVo.CancelMsgApply cancelMsgApply = new AuditWheelCancelCheckVo.CancelMsgApply();
                cancelMsgApply.setUniqId(auditWheelMessageCancel.getUniqueId());
                cancelMsgApply.setFormatTime(auditWheelMessageCancel.getDeliverTime());
                cancelMsgApply.setStatus(notCancelIds.contains(auditWheelMessageCancel.getUniqueId()));
                cancelMsgApply.setIndex(index++);
                cancelCheckVo.addCancelMsgApply(cancelMsgApply);
            }
            return Result.getResult(cancelCheckVo);
        } catch (Exception e) {
            logger.error("checkCancel err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 检查申请是否重复
     *
     * @param tid
     * @param uniqIds
     * @return Result<List<String>>
     */
    public Result<List<String>> checkExistsByUniqIdAndTopic(long tid, List<String> uniqIds) {
        try {
            List<AuditWheelMessageCancel> auditWheelMessageCancels = auditWheelMessageCancelDao.selectByUniqIdAndTid(tid, uniqIds, Audit.StatusEnum.INIT.getStatus());
            if (auditWheelMessageCancels == null || auditWheelMessageCancels.isEmpty()) {
                return Result.getResult(new ArrayList<>());
            }
            List<String> existUniqIds = auditWheelMessageCancels.stream().map(AuditWheelMessageCancel::getUniqueId).collect(Collectors.toList());
            return Result.getResult(existUniqIds);
        } catch (Exception e) {
            logger.error("CheckExistsByUniqIdAndTopic err, tid:{}, uniqIds:{}", tid, uniqIds, e);
            return Result.getDBErrorResult(e);
        }
    }
}
