package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Controller;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.dao.ControllerDao;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

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
        try {
            return Result.getResult(controllerDao.insert(cid, addr, baseDir));
        } catch (Exception e) {
            logger.error("insert err, cid:{}, addr:{}, baseDir:{}", cid, addr, baseDir, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询集群列表
     *
     * @return Result<List<Controller>>
     */
    public Result<List<Controller>> query(int cid) {
        try {
            return Result.getResult(controllerDao.selectByClusterId(cid));
        } catch (Exception e) {
            logger.error("query cid:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询全部Controller
     *
     * @return Result<List<Controller>>
     */
    public Result<List<Controller>> queryAll() {
        try {
            return Result.getResult(controllerDao.selectAll());
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
            return Result.getResult(controllerDao.delete(cid, addr));
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
            return Result.getResult(controllerDao.update(cid, addr, checkStatusEnum.getStatus()));
        } catch (Exception e) {
            logger.error("update err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 健康检查
     *
     * @param cluster
     * @param addr
     * @return
     */
    public Result<?> healthCheck(Cluster cluster, String addr) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                try {
                    SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                    sohuMQAdmin.getControllerMetaData(addr);
                    return Result.getOKResult();
                } catch (Exception e) {
                    return Result.getDBErrorResult(e).setMessage("addr:" + addr + ";Exception: " + e.getMessage());
                }
            }

            public Cluster mqCluster() {
                return cluster;
            }

            @Override
            public Result<?> exception(Exception e) throws Exception {
                return Result.getDBErrorResult(e).setMessage("Exception: " + e.getMessage());
            }
        });
    }
}
