package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.NameServer;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.MQDeployer;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * name server
 * 
 * @author yongfeigao
 * @date 2018年10月23日
 */
@Controller
@RequestMapping("/admin/nameserver")
public class AdminNameServerController extends AdminViewController {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private NameServerService nameServerService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private MQDeployer mqDeployer;

    @RequestMapping("/list")
    public String list(@RequestParam(name = "cid", required = false) Integer cid, Map<String, Object> map) {
        setView(map, "list");
        Cluster mqCluster = getMQCluster(cid);
        if (mqCluster == null) {
            return view();
        }
        Result<List<NameServer>> result = nameServerService.query(mqCluster.getId());
        setResult(map, result);
        setResult(map, "clusters", clusterService.getAllMQCluster());
        setResult(map, "selectedCluster", mqCluster);
        setResult(map, "username", mqCloudConfigHelper.getServerUser());
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
        if(addrs.length != 2) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        String ip = addrs[0];
        String portStr = addrs[1];
        int port = NumberUtils.toInt(portStr);
        if (port == 0) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Result<?> portResult = mqDeployer.getListenPortInfo(ip, port);
        if(portResult.getStatus() != Status.DB_ERROR.getKey()) {
            return Result.getResult(Status.NO_RESULT);
        }
        Result<?> result = nameServerService.save(cid, addr);
        return Result.getWebResult(result);
    }
    
    /**
     * 下线
     * 
     * @param cid
     * @param broker
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
        Result<?> result = mqDeployer.shutdown(ip, port);
        if (result.isOK()) {
            nameServerService.delete(cid, addr);
        }
        return result;
    }
    
    /**
     * 删除
     * 
     * @param cid
     * @param broker
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Result<?> delete(UserInfo ui, @RequestParam(name = "addr") String addr,
            @RequestParam(name = "cid") int cid) {
        logger.warn("offline:{}, user:{}", addr, ui);
        Result<?> result = nameServerService.delete(cid, addr);
        return Result.getWebResult(result);
    }

    /**
     * 启动
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/startup", method = RequestMethod.POST)
    public Result<?> startup(UserInfo ui, @RequestParam(name = "ip") String ip, @RequestParam(name = "port") int port,
            @RequestParam(name = "dir") String dir, @RequestParam(name = "cid") int cid) {
        logger.warn("startup, ip:{}, dir:{}, user:{}", ip, dir, ui);
        Result<?> result = mqDeployer.startup(ip, dir);
        if (result.isOK()) {
            nameServerService.save(cid, ip + ":" + port);
        }
        return result;
    }

    private Cluster getMQCluster(Integer cid) {
        Cluster mqCluster = null;
        if (cid != null) {
            mqCluster = clusterService.getMQClusterById(cid);
        }
        if (mqCluster == null && clusterService.getAllMQCluster() != null) {
            mqCluster = clusterService.getAllMQCluster()[0];
        }
        return mqCluster;
    }

    @Override
    public String viewModule() {
        return "nameserver";
    }
}
