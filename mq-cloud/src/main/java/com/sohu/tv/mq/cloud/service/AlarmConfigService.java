package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.dao.AlarmConfigDao;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

/**
 * 预警配置项
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月26日
 */
@Service
public class AlarmConfigService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AlarmConfigDao alarmConfigDao;

    /**
     * 查询所有用户报警配置
     * 
     * @return
     */
    public Result<List<AlarmConfig>> queryUserAlarmConfig() {
        List<AlarmConfig> list = null;
        try {
            list = alarmConfigDao.selectUserAlarmConfig();
        } catch (Exception e) {
            logger.error("queryAll", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 根据UID查询所有报警配置
     * 
     * @return
     */
    public Result<List<AlarmConfig>> queryByUid(long uid) {
        List<AlarmConfig> result = null;
        try {
            result = alarmConfigDao.selectByUid(uid);
        } catch (Exception e) {
            logger.error("queryByUID", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 根据ID查询报警配置
     * 
     * @return
     */
    public Result<AlarmConfig> queryByID(long id) {
        AlarmConfig result = null;
        try {
            result = alarmConfigDao.selectByID(id);
        } catch (Exception e) {
            logger.error("queryByID", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 根据ID查询修改报警配置
     * 
     * @return
     */
    public Result<AlarmConfig> updateByID(AlarmConfig alarmConfig) {
        Integer count = null;
        try {
            count = alarmConfigDao.update(alarmConfig);
        } catch (Exception e) {
            logger.error("update err, alarmConfig:{}", alarmConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 根据ID删除报警配置
     * 
     * @return
     */
    public Result<?> deleteByID(long id) {
        Integer count = null;
        try {
            count = alarmConfigDao.deleteByID(id);
            if (count == null || count != 1) {
                return Result.getResult(Status.DB_ERROR);
            }
        } catch (Exception e) {
            logger.error("deleteByID err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 根据ID删除报警配置
     * 
     * @return
     */
    public Result<?> save(AlarmConfig alarmConfig) {
        Integer count = null;
        try {
            count = alarmConfigDao.insert(alarmConfig);
            if (count == null || count != 1) {
                return Result.getResult(Status.DB_ERROR);
            }
        } catch (Exception e) {
            logger.error("save err, alarmConfig:{}", alarmConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
}
