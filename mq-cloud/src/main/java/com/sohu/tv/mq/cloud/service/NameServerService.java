package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.bo.NameServer;
import com.sohu.tv.mq.cloud.dao.NameServerDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * name server
 * 
 * @author yongfeigao
 * @date 2018年10月23日
 */
@Service
public class NameServerService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private NameServerDao nameServerDao;
    
    /**
     * 保存记录
     * 
     * @param nameServer
     * @return 返回Result
     */
    public Result<?> save(int cid, String addr) {
        return save(cid, addr, null);
    }
    
    /**
     * 保存记录
     * 
     * @param nameServer
     * @return 返回Result
     */
    public Result<?> save(int cid, String addr, String baseDir) {
        Integer result = null;
        try {
            result = nameServerDao.insert(cid, addr, baseDir);
        } catch (Exception e) {
            logger.error("insert err, cid:{}, addr:{}, baseDir:{}", cid, addr, baseDir, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 查询集群的name server
     * 
     * @return Result<List<NameServer>>
     */
    public Result<List<NameServer>> query(int cid) {
        List<NameServer> result = null;
        try {
            result = nameServerDao.selectByClusterId(cid);
        } catch (Exception e) {
            logger.error("query cid:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 查询全部name server
     * 
     * @return Result<List<NameServer>>
     */
    public Result<List<NameServer>> queryAll() {
        List<NameServer> result = null;
        try {
            result = nameServerDao.selectAll();
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
            result = nameServerDao.delete(cid, addr);
        } catch (Exception e) {
            logger.error("delete err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    
    /**
     * 更新记录
     * @param cid
     * @param addr
     * @return
     */
    public Result<?> update(int cid, String addr, CheckStatusEnum checkStatusEnum) {
        Integer result = null;
        try {
            result = nameServerDao.update(cid, addr, checkStatusEnum.getStatus());
        } catch (Exception e) {
            logger.error("update err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
}
