package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Proxy;
import com.sohu.tv.mq.cloud.common.model.ClientConnectionSize;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.MQDeployer;
import com.sohu.tv.mq.cloud.service.ProxyService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * proxy
 *
 * @author yongfeigao
 * @date 2023年05月25日
 */
@org.springframework.stereotype.Controller
@RequestMapping("/admin/proxy")
public class AdminProxyController extends AdminViewController {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private MQDeployer mqDeployer;

    @RequestMapping("/list")
    public String list(@RequestParam(name = "cid", required = false) Integer cid, Map<String, Object> map) {
        setView(map, "list");
        Cluster mqCluster = clusterService.getOrDefaultMQCluster(cid);
        if (mqCluster == null) {
            return view();
        }
        Result<List<Proxy>> result = proxyService.query(mqCluster.getId());
        if (result.isNotEmpty()) {
            // 检查状态
            result.getResult().forEach(proxy -> {
                Result<?> healthCheckResult = proxyService.healthCheck(proxy.getAddr());
                if (healthCheckResult.isOK()) {
                    proxy.setCheckStatus(CheckStatusEnum.OK.getStatus());
                    ClientConnectionSize connSize = proxyService.getClientConnectionSize(cid, proxy.getAddr()).getResult();
                    if (connSize != null) {
                        proxy.setProducerSize(connSize.getProducerSize());
                        proxy.setProducerConnectionSize(connSize.getProducerConnectionSize());
                        proxy.setConsumerSize(connSize.getConsumerSize());
                        proxy.setConsumerConnectionSize(connSize.getConsumerConnectionSize());
                    }
                } else {
                    proxy.setCheckStatus(CheckStatusEnum.FAIL.getStatus());
                }
            });
        }
        setResult(map, result);
        setResult(map, "clusters", clusterService.getAllMQCluster());
        setResult(map, "selectedCluster", mqCluster);
        setResult(map, "username", mqCloudConfigHelper.getServerUser());
        setResult(map, "domain", mqCloudConfigHelper.getDomain());
        return view();
    }

    /**
     * 关联
     *
     * @param cid
     * @param broker
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(UserInfo ui, @RequestParam(name = "addr") String addr,
                         @RequestParam(name = "cid") int cid) {
        String[] addrs = addr.split(":");
        if (addrs.length != 2) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        String ip = addrs[0];
        String portStr = addrs[1];
        int port = NumberUtils.toInt(portStr);
        if (port == 0) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Result<?> portResult = mqDeployer.getListenPortInfo(ip, port);
        if (portResult.getStatus() != Status.DB_ERROR.getKey()) {
            return Result.getResult(Status.NO_RESULT);
        }
        Proxy proxy = new Proxy();
        proxy.setCid(cid);
        proxy.setAddr(addr);
        Result<?> result = proxyService.save(proxy);
        return Result.getWebResult(result);
    }

    /**
     * 下线
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/offline", method = RequestMethod.POST)
    public Result<?> offline(UserInfo ui, @RequestParam(name = "addr") String addr,
                             @RequestParam(name = "cid") int cid) {
        logger.warn("offline:{}, user:{}", addr, ui);
        String[] addrs = addr.split(":");
        String ip = addrs[0];
        String portStr = addrs[1];
        int port = NumberUtils.toInt(portStr);
        if (port == 0) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        return mqDeployer.shutdown(ip, port);
    }

    /**
     * 删除
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Result<?> delete(UserInfo ui, @RequestParam(name = "addr") String addr,
                            @RequestParam(name = "cid") int cid) {
        logger.warn("offline:{}, user:{}", addr, ui);
        Result<?> result = proxyService.delete(cid, addr);
        return Result.getWebResult(result);
    }

    /**
     * 启动
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/startup", method = RequestMethod.POST)
    public Result<?> startup(UserInfo ui, @RequestParam(name = "ip") String ip,
                             @RequestParam(name = "listenPort") int port,
                             @RequestParam(name = "dir") String dir,
                             @RequestParam(name = "cid") int cid,
                             @RequestParam(name = "config", required = false) String config) {
        logger.warn("startup, ip:{}, dir:{}, user:{}", ip, dir, ui);
        Result<?> result = mqDeployer.startup(ip, dir, port);
        if (result.isOK() && config != null) {
            Proxy proxy = new Proxy();
            proxy.setCid(cid);
            proxy.setBaseDir(dir);
            proxy.setAddr(ip + ":" + port);
            proxy.setConfig(config);
            proxyService.save(proxy);
        }
        return result;
    }

    /**
     * producer链接
     */
    @GetMapping(value = "/producer/connection")
    public String producerConnection(@RequestParam(name = "cid") int cid,
                                     @RequestParam(name = "addr") String addr,
                                     Map<String, Object> map) {
        setResult(map, proxyService.fetchAllProducerConnection(addr, clusterService.getMQClusterById(cid)));
        return adminViewModule() + "/clientConnectionInfo";
    }

    /**
     * consumer链接
     */
    @GetMapping(value = "/consumer/connection")
    public String consumerConnection(@RequestParam(name = "cid") int cid,
                                     @RequestParam(name = "addr") String addr,
                                     Map<String, Object> map) {
        setResult(map, proxyService.fetchAllConsumerConnection(addr, clusterService.getMQClusterById(cid)));
        return adminViewModule() + "/clientConnectionInfo";
    }

    /**
     * 剔除流量
     */
    @ResponseBody
    @RequestMapping(value = "/unregister", method = RequestMethod.POST)
    public Result<?> unregister(UserInfo ui, @RequestParam(name = "addr") String addr, @RequestParam(name = "cid") int cid) {
        logger.warn("unregister:{}, user:{}", addr, ui);
        return Result.getWebResult(proxyService.updateStatus(cid, addr, 1));
    }

    /**
     * 恢复流量
     */
    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Result<?> register(UserInfo ui, @RequestParam(name = "addr") String addr, @RequestParam(name = "cid") int cid) {
        logger.warn("register:{}, user:{}", addr, ui);
        return Result.getWebResult(proxyService.updateStatus(cid, addr, 0));
    }

    @Override
    public String viewModule() {
        return "proxy";
    }
}
