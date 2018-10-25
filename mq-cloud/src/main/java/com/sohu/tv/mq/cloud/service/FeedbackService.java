package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.Feedback;
import com.sohu.tv.mq.cloud.dao.FeedbackDao;
import com.sohu.tv.mq.cloud.util.Result;
/**
 * 反馈服务
 * 
 * @author yongfeigao
 * @date 2018年9月18日
 */
@Service
public class FeedbackService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private FeedbackDao feedbackDao;
    
    /**
     * 保存feedback记录
     * 
     * @param feedback
     * @return 返回Result
     */
    public Result<?> save(Feedback feedback) {
        try {
            feedbackDao.insert(feedback);
        } catch (Exception e) {
            logger.error("insert err, feedback:{}", feedback, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 查询所有feedback
     * 
     * @return Result<List<Feedback>>
     */
    public Result<List<Feedback>> queryAll() {
        List<Feedback> feedbackList = null;
        try {
            feedbackList = feedbackDao.selectAll();
        } catch (Exception e) {
            logger.error("queryAll err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(feedbackList);
    }
}
