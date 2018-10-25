package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.Notice;
import com.sohu.tv.mq.cloud.dao.NoticeDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 通知服务
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
@Service
public class NoticeService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private NoticeDao noticeDao;
    
    /**
     * 保存notice记录
     * 
     * @param notice
     * @return 返回Result
     */
    public Result<?> save(Notice notice) {
        try {
            noticeDao.insert(notice);
        } catch (Exception e) {
            logger.error("insert err, notice:{}", notice, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 查询当前的notice
     * 
     * @return Result<Notice>
     */
    public Result<Notice> queryNow() {
        Notice notice = null;
        try {
            notice = noticeDao.selectNow();
        } catch (Exception e) {
            logger.error("selectNow err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(notice);
    }
    
    /**
     * 查询所有的notice
     * 
     * @return Result<List<Notice>>
     */
    public Result<List<Notice>> queryAll() {
        List<Notice> noticeList = null;
        try {
            noticeList = noticeDao.select();
        } catch (Exception e) {
            logger.error("queryAll err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(noticeList);
    }
    
    /**
     * 更新notice记录
     * 
     * @param notice
     * @return 返回Result
     */
    public Result<?> update(Notice notice) {
        try {
            noticeDao.update(notice);
        } catch (Exception e) {
            logger.error("update err, notice:{}", notice, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 删除notice记录
     * 
     * @param notice
     * @return 返回Result
     */
    public Result<?> delete(long id) {
        try {
            noticeDao.delete(id);
        } catch (Exception e) {
            logger.error("delete err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
}
