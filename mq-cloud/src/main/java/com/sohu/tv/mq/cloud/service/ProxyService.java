package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.bo.ClientConnectionInfo;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Proxy;
import com.sohu.tv.mq.cloud.common.model.ClientConnectionSize;
import com.sohu.tv.mq.cloud.common.model.ConsumerTableInfo;
import com.sohu.tv.mq.cloud.dao.ProxyDao;
import com.sohu.tv.mq.cloud.mq.DefaultSohuMQAdmin;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.apache.rocketmq.remoting.protocol.body.ProducerTableInfo;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import static com.sohu.tv.mq.cloud.bo.DeployableComponent.STATUS_ERROR;
import static com.sohu.tv.mq.cloud.bo.DeployableComponent.STATUS_OK;

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

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private ClusterService clusterService;

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
     * 查询集群列表
     *
     * @return Result<List<Proxy>>
     */
    public Result<List<Proxy>> queryOK(int cid) {
        try {
            return Result.getResult(proxyDao.selectOKByClusterId(cid));
        } catch (Exception e) {
            logger.error("queryOK cid:{} err", cid, e);
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

    public Result<?> updateStatusOK(int cid, String addr) {
        return updateStatus(cid, addr, STATUS_OK);
    }

    public Result<?> updateStatusError(int cid, String addr) {
        return updateStatus(cid, addr, STATUS_ERROR);
    }

    public Result<?> updateStatus(int cid, String addr, int status) {
        try {
            return Result.getResult(proxyDao.updateStatus(cid, addr, status));
        } catch (Exception e) {
            logger.error("updateStatus err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询proxy
     */
    public Result<Proxy> query(String addr) {
        try {
            return Result.getResult(proxyDao.selectByAddr(addr));
        } catch (Exception e) {
            logger.error("query addr:{} err", addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 获取proxy的连接数
     */
    public Result<ClientConnectionSize> getClientConnectionSize(Integer cid, String addr) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<ClientConnectionSize>>() {
            public Result<ClientConnectionSize> callback(MQAdminExt mqAdmin) throws Exception {
                DefaultSohuMQAdmin sohuMQAdmin = (DefaultSohuMQAdmin) mqAdmin;
                return Result.getResult(sohuMQAdmin.getClientConnectionSize(addr));
            }

            public Result<ClientConnectionSize> exception(Exception e) throws Exception {
                logger.error("getClientConnectionSize {} err", addr, e);
                return Result.getDBErrorResult(e);
            }

            public Cluster mqCluster() {
                return clusterService.getOrDefaultMQCluster(cid);
            }

            public boolean isProxyRemoting() {
                return true;
            }
        });
    }

    /**
     * 查询producer连接
     */
    public Result<ClientConnectionInfo> fetchAllProducerConnection(String addr, Cluster mqCluster) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<ClientConnectionInfo>>() {
            public Result<ClientConnectionInfo> callback(MQAdminExt mqAdmin) throws Exception {
                DefaultSohuMQAdmin sohuMQAdmin = (DefaultSohuMQAdmin) mqAdmin;
                ProducerTableInfo producerTableInfo = sohuMQAdmin.getAllProducerInfo(addr, true);
                return Result.getResult(ClientConnectionInfo.build(producerTableInfo));
            }

            public Cluster mqCluster() {
                return mqCluster;
            }

            public Result<ClientConnectionInfo> exception(Exception e) throws Exception {
                logger.error("fetchAllProducerConnection:{} err", addr, e);
                return Result.getDBErrorResult(e);
            }

            public boolean isProxyRemoting() {
                return true;
            }
        });
    }

    /**
     * 查询consumer连接
     */
    public Result<ClientConnectionInfo> fetchAllConsumerConnection(String addr, Cluster mqCluster) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<ClientConnectionInfo>>() {
            public Result<ClientConnectionInfo> callback(MQAdminExt mqAdmin) throws Exception {
                DefaultSohuMQAdmin sohuMQAdmin = (DefaultSohuMQAdmin) mqAdmin;
                ConsumerTableInfo consumerTableInfo = sohuMQAdmin.getAllConsumerInfo(addr, true);
                return Result.getResult(ClientConnectionInfo.build(consumerTableInfo));
            }

            public Cluster mqCluster() {
                return mqCluster;
            }

            public Result<ClientConnectionInfo> exception(Exception e) throws Exception {
                logger.error("fetchAllConsumerConnection:{} err", addr, e);
                return Result.getDBErrorResult(e);
            }

            public boolean isProxyRemoting() {
                return true;
            }
        });
    }
}
