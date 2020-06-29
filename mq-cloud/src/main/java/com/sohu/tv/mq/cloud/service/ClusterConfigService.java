package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.ClusterConfig;
import com.sohu.tv.mq.cloud.dao.ClusterConfigDao;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

/**
 * 集群配置
 * 
 * @author yongfeigao
 * @date 2020年5月19日
 */
@Service
public class ClusterConfigService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClusterConfigDao clusterConfigDao;

    /**
     * 保存
     * 
     * @return
     */
    public Result<Integer> save(ClusterConfig clusterConfig) {
        Integer result = null;
        try {
            result = clusterConfigDao.insert(clusterConfig);
        } catch(DuplicateKeyException e) {
            logger.warn("save:{}", clusterConfig);
            return Result.getResult(Status.DB_DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("save:{}", clusterConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 删除
     * 
     * @return
     */
    public Result<Integer> delete(int cid, int bid) {
        Integer result = null;
        try {
            result = clusterConfigDao.delete(cid, bid);
        } catch (Exception e) {
            logger.error("delete cid:{}, bid:{}", cid, bid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 修改
     * 
     * @return
     */
    public Result<Integer> update(ClusterConfig clusterConfig) {
        Integer result = null;
        try {
            result = clusterConfigDao.update(clusterConfig);
        } catch (Exception e) {
            logger.error("update clusterConfig:{}", clusterConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 查询
     * 
     * @return
     */
    public Result<List<ClusterConfig>> query(int cid) {
        List<ClusterConfig> list = null;
        try {
            list = clusterConfigDao.select(cid);
        } catch (Exception e) {
            logger.error("query", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
}
