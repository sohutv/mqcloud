package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Controller;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ControllerService;
import com.sohu.tv.mq.cloud.service.MQDeployer;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * controller
 *
 * @author yongfeigao
 * @date 2023年05月25日
 */
@org.springframework.stereotype.Controller
@RequestMapping("/admin/controller")
public class AdminControllerController extends AdminViewController {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ControllerService controllerService;

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
        Result<List<Controller>> result = controllerService.query(mqCluster.getId());
        if (result.isNotEmpty()) {
            // 检查状态
            result.getResult().forEach(controller -> {
                Result<?> healthCheckResult = controllerService.healthCheck(mqCluster, controller.getAddr());
                if (healthCheckResult.isOK()) {
                    controller.setCheckStatus(CheckStatusEnum.OK.getStatus());
                } else {
                    controller.setCheckStatus(CheckStatusEnum.FAIL.getStatus());
                }
            });
        }
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
        Result<?> result = controllerService.save(cid, addr);
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
        return mqDeployer.shutdown(ip, port);
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
        Result<?> result = controllerService.delete(cid, addr);
        return Result.getWebResult(result);
    }

    /**
     * 启动
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/startup", method = RequestMethod.POST)
    public Result<?> startup(UserInfo ui, @RequestParam(name = "ip") String ip, @RequestParam(name = "listenPort") int port,
                             @RequestParam(name = "dir") String dir, @RequestParam(name = "cid") int cid) {
        logger.warn("startup, ip:{}, dir:{}, user:{}", ip, dir, ui);
        Result<?> result = mqDeployer.startup(ip, dir, port);
        if (result.isOK()) {
            controllerService.save(cid, ip + ":" + port, dir);
        }
        return result;
    }

    @Override
    public String viewModule() {
        return "controller";
    }
}
