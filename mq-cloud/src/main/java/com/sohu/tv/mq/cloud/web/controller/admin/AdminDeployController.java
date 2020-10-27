package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohu.tv.mq.cloud.service.MQDeployer;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 部署控制器
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月18日
 */
@RestController
@RequestMapping("/admin/deploy")
public class AdminDeployController extends AdminViewController {
    
    @Autowired
    private MQDeployer mqDeployer;
    
    /**
     * 校验jdk
     * @return
     */
    @RequestMapping("/check/jdk")
    public Result<?> checkJdk(@RequestParam(name="ip") String ip) {
        return mqDeployer.getJDKVersion(ip);
    }
    
    /**
     * 检查端口
     * @return
     */
    @RequestMapping(value="/check/port", method=RequestMethod.POST)
    public Result<?> checkPort(UserInfo ui, @RequestParam(name="ip") String ip, @RequestParam(name="listenPort") int port) {
        // 获取监听的端口的信息
        Result<?> portResult = mqDeployer.getListenPortInfo(ip, port);
        return portResult;
    }
    
    /**
     * 检查目录
     * @return
     */
    @RequestMapping(value="/check/dir", method=RequestMethod.POST)
    public Result<?> checkDir(UserInfo ui, @RequestParam(name="ip") String ip, @RequestParam(name="dir") String dir) {
        // 获取目录是否存在
        Result<?> dirResult = mqDeployer.dirWrite(ip, dir);
        return dirResult;
    }
    
    /**
     * 查询进程信息
     * @param cid
     * @param broker
     * @return
     */
    @RequestMapping("/check/program")
    public Result<?> checkProgram(@RequestParam(name="ip") String ip, @RequestParam(name="listenPort") int port) {
        return mqDeployer.getProgram(ip, port);
    }
    
    /**
     * 查询进程信息
     * @param cid
     * @param broker
     * @return
     */
    @RequestMapping("/program")
    public Result<?> program(@RequestParam(name="addr") String addr) {
        String[] addrs = addr.split(":");
        String ip = addrs[0];
        String portStr = addrs[1];
        int port = NumberUtils.toInt(portStr);
        if(port == 0) {
            return Result.getResult(Status.NO_RESULT);
        }
        return mqDeployer.getProgram(ip, port);
    }
    
    /**
     * 拷贝
     * @return
     */
    @RequestMapping(value="/scp", method=RequestMethod.POST)
    public Result<?> scp(UserInfo ui, @RequestParam(name="ip") String ip) {
        logger.warn("scp:{}, user:{}", ip, ui);
        // 远程拷贝文件
        Result<?> wgetResult = mqDeployer.scp(ip);
        if(wgetResult.isNotOK()) {
            return wgetResult;
        }
        return Result.getOKResult();
    }
    
    /**
     * 解压
     * @return
     */
    @RequestMapping(value="/unzip", method=RequestMethod.POST)
    public Result<?> unzip(UserInfo ui, @RequestParam(name="ip") String ip, @RequestParam(name="dir") String dir) {
        logger.warn("unzip:{}, dir:{}, user:{}", ip, dir, ui);
        // 解压
        Result<?> unzipResult = mqDeployer.unzip(ip, dir);
        if(unzipResult.isNotOK()) {
            return unzipResult;
        }
        return Result.getOKResult();
    }
    
    /**
     * 配置
     * @return
     */
    @RequestMapping(value="/config/ns", method=RequestMethod.POST)
    public Result<?> configNS(UserInfo ui, @RequestParam(name="ip") String ip, @RequestParam(name="listenPort") int port, 
            @RequestParam(name="dir") String dir) {
        logger.warn("configNS:{}, dir:{}, port:{}, user:{}", ip, dir, port, ui);
        // 配置
        Result<?> configResult = mqDeployer.configNameServer(ip, port, dir);
        if(configResult.isNotOK()) {
            return configResult;
        }
        return Result.getOKResult();
    }
    
    /**
     * 配置
     * @return
     */
    @RequestMapping(value="/config/broker", method=RequestMethod.POST)
    public Result<?> configBroker(UserInfo ui, @RequestParam Map<String, Object> param) {
        logger.warn("configBroker, brokerParam:{}, user:{}", param, ui);
        // 配置
        Result<?> configResult = mqDeployer.configBroker(param);
        if(configResult.isNotOK()) {
            return configResult;
        }
        return Result.getOKResult();
    }
    
    /**
     * 配置
     * @return
     */
    @RequestMapping(value="/config/init", method=RequestMethod.POST)
    public Result<?> initConfig(UserInfo ui, @RequestParam(name="ip") String ip, @RequestParam(name="dir") String dir) {
        logger.warn("initConfig, ip:{}, dir:{}, user:{}", ip, dir, ui);
        // 配置
        Result<?> configResult = mqDeployer.initConfig(ip, dir);
        if(configResult.isNotOK()) {
            return configResult;
        }
        return Result.getOKResult();
    }
    
    /**
     * 启动
     * @return
     */
    @RequestMapping(value="/startup", method=RequestMethod.POST)
    public Result<?> startup(UserInfo ui, @RequestParam(name="ip") String ip, @RequestParam(name="dir") String dir) {
        logger.warn("startup, ip:{}, dir:{}, user:{}", ip, dir, ui);
        return mqDeployer.startup(ip, dir);
    }
    
    /**
     * 启动，校验port方式重复启动
     * 
     * @return
     */
    @RequestMapping(value = "/startup/broker", method = RequestMethod.POST)
    public Result<?> startupBroker(UserInfo ui, @RequestParam(name = "ipAddr") String ipAddr,
            @RequestParam(name = "dir") String dir) {
        logger.warn("startup, ipAddr:{}, dir:{}, user:{}", ipAddr, dir, ui);
        // 参数校验
        String[] ips = ipAddr.split(":");
        if (ips.length != 2) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        // 校验端口
        String ip = ips[0];
        int port = NumberUtils.toInt(ips[1]);
        Result<?> result = mqDeployer.getListenPortInfo(ip, port);
        if (result.isNotOK()) {
            // 端口被占
            if (result.getStatus() == Status.DB_ERROR.getKey()) {
                if (result.getResult() != null) {
                    result.setMessage(result.getResult().toString() + "端口被占用");
                }
            }
            return result;
        }
        // 启动
        return mqDeployer.startup(ip, dir);
    }
    
    /**
     * 下线
     * @param cid
     * @param broker
     * @return
     */
    @RequestMapping(value="/shutdown", method=RequestMethod.POST)
    public Result<?> shutdown(UserInfo ui, @RequestParam(name="addr") String addr) {
        logger.warn("shutdown:{}, user:{}", addr, ui);
        String[] addrs = addr.split(":");
        String ip = addrs[0];
        String portStr = addrs[1];
        int port = NumberUtils.toInt(portStr);
        if(port == 0) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        return mqDeployer.shutdown(ip, port);
    }
    
    @Override
    public String viewModule() {
        return "deploy";
    }
}
