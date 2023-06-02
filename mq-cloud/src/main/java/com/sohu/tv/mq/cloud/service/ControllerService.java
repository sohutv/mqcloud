package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.bo.Controller;
import com.sohu.tv.mq.cloud.dao.ControllerDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ControllerService
 *
 * @author yongfeigao
 * @date 2023年05月22日
 */
@Service
public class ControllerService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ControllerDao controllerDao;

    /**
     * 保存记录
     *
     * @return 返回Result
     */
    public Result<?> save(int cid, String addr) {
        return save(cid, addr, null);
    }

    /**
     * 保存记录
     *
     * @return 返回Result
     */
    public Result<?> save(int cid, String addr, String baseDir) {
        Integer result = null;
        try {
            result = controllerDao.insert(cid, addr, baseDir);
        } catch (Exception e) {
            logger.error("insert err, cid:{}, addr:{}, baseDir:{}", cid, addr, baseDir, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 查询集群列表
     *
     * @return Result<List<Controller>>
     */
    public Result<List<Controller>> query(int cid) {
        List<Controller> result = null;
        try {
            result = controllerDao.selectByClusterId(cid);
        } catch (Exception e) {
            logger.error("query cid:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 查询全部Controller
     *
     * @return Result<List<Controller>>
     */
    public Result<List<Controller>> queryAll() {
        List<Controller> result = null;
        try {
            result = controllerDao.selectAll();
        } catch (Exception e) {
            logger.error("query all err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 删除记录
     *
     * @return 返回Result
     */
    public Result<?> delete(int cid, String addr) {
        Integer result = null;
        try {
            result = controllerDao.delete(cid, addr);
        } catch (Exception e) {
            logger.error("delete err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }


    /**
     * 更新记录
     *
     * @param cid
     * @param addr
     * @return
     */
    public Result<?> update(int cid, String addr, CheckStatusEnum checkStatusEnum) {
        Integer result = null;
        try {
            result = controllerDao.update(cid, addr, checkStatusEnum.getStatus());
        } catch (Exception e) {
            logger.error("update err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
}
