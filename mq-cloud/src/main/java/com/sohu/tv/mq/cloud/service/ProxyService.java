package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.bo.Controller;
import com.sohu.tv.mq.cloud.bo.Proxy;
import com.sohu.tv.mq.cloud.dao.ControllerDao;
import com.sohu.tv.mq.cloud.dao.ProxyDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ProxyService
 *
 * @author yongfeigao
 * @date 2023年05月25日
 */
@Service
public class ProxyService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProxyDao proxyDao;

    /**
     * 保存记录
     *
     * @return 返回Result
     */
    public Result<?> save(Proxy proxy) {
        Integer result = null;
        try {
            result = proxyDao.insert(proxy);
        } catch (Exception e) {
            logger.error("insert err {}", proxy, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 查询集群列表
     *
     * @return Result<List<Proxy>>
     */
    public Result<List<Proxy>> query(int cid) {
        List<Proxy> result = null;
        try {
            result = proxyDao.selectByClusterId(cid);
        } catch (Exception e) {
            logger.error("query cid:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 查询全部Proxy
     *
     * @return Result<List<Proxy>>
     */
    public Result<List<Proxy>> queryAll() {
        List<Proxy> result = null;
        try {
            result = proxyDao.selectAll();
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
            result = proxyDao.delete(cid, addr);
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
            result = proxyDao.update(cid, addr, checkStatusEnum.getStatus());
        } catch (Exception e) {
            logger.error("update err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
}
