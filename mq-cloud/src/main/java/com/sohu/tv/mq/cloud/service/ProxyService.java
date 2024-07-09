package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Controller;
import com.sohu.tv.mq.cloud.bo.Proxy;
import com.sohu.tv.mq.cloud.dao.ControllerDao;
import com.sohu.tv.mq.cloud.dao.ProxyDao;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
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
        try {
            return Result.getResult(proxyDao.insert(proxy));
        } catch (Exception e) {
            logger.error("insert err {}", proxy, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询集群列表
     *
     * @return Result<List<Proxy>>
     */
    public Result<List<Proxy>> query(int cid) {
        try {
            return Result.getResult(proxyDao.selectByClusterId(cid));
        } catch (Exception e) {
            logger.error("query cid:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询全部Proxy
     *
     * @return Result<List<Proxy>>
     */
    public Result<List<Proxy>> queryAll() {
        try {
            return Result.getResult(proxyDao.selectAll());
        } catch (Exception e) {
            logger.error("query all err", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 删除记录
     *
     * @return 返回Result
     */
    public Result<?> delete(int cid, String addr) {
        try {
            return Result.getResult(proxyDao.delete(cid, addr));
        } catch (Exception e) {
            logger.error("delete err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
    }


    /**
     * 更新记录
     *
     * @param cid
     * @param addr
     * @return
     */
    public Result<?> update(int cid, String addr, CheckStatusEnum checkStatusEnum) {
        try {
            return Result.getResult(proxyDao.update(cid, addr, checkStatusEnum.getStatus()));
        } catch (Exception e) {
            logger.error("update err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 健康检查
     *
     * @param addr
     * @return
     */
    public Result<?> healthCheck(String addr) {
        try (Socket socket = new Socket()) {
            String[] addrs = addr.split(":");
            socket.connect(new InetSocketAddress(addrs[0], Integer.parseInt(addrs[1])), 3000);
            if (socket.isConnected()) {
                return Result.getOKResult();
            } else {
                return Result.getResult(Status.NO_RESULT).setMessage("addr:" + addr + " is not connected");
            }
        } catch (Exception e) {
            return Result.getDBErrorResult(e).setMessage("addr:" + addr + ";Exception: " + e.getMessage());
        }
    }
}
