package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.DataMigrationService;
import com.sohu.tv.mq.cloud.service.MQDeployer;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.RocketMQVersion;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.StoreFileParam;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

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

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private DataMigrationService dataMigrationService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
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
     * 检查目录
     * @return
     */
    @RequestMapping(value="/check/dir/exist", method=RequestMethod.POST)
    public Result<?> checkDirExist(UserInfo ui, @RequestParam(name="destIp") String ip, @RequestParam(name="destHome") String dir) {
        return mqDeployer.dirExist(ip, dir);
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
    @RequestMapping(value = "/scp", method = RequestMethod.POST)
    public Result<?> scp(UserInfo ui, @RequestParam(name = "ip") String ip, @RequestParam(name = "v") String version) {
        logger.warn("scp:{}, user:{}", ip, ui);
        // 远程拷贝文件
        Result<?> wgetResult = mqDeployer.scp(ip, RocketMQVersion.getRocketMQVersion(version));
        if (wgetResult.isNotOK()) {
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
    public Result<?> configNS(UserInfo ui, @RequestParam Map<String, Object> param) {
        logger.warn("configNS, param, user:{}", param, ui);
        // 配置
        Result<?> configResult = mqDeployer.configNameServer(param);
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
        brokerService.saveBrokerTmp(param);
        return Result.getOKResult();
    }

    /**
     * 配置
     * @return
     */
    @RequestMapping(value="/config/controller", method=RequestMethod.POST)
    public Result<?> configController(UserInfo ui, @RequestParam Map<String, Object> param) {
        logger.warn("configController:{}, param:{}", param, ui);
        // 配置
        Result<?> configResult = mqDeployer.configController(param);
        if(configResult.isNotOK()) {
            return configResult;
        }
        return Result.getOKResult();
    }

    /**
     * 配置
     * @return
     */
    @RequestMapping(value="/config/proxy", method=RequestMethod.POST)
    public Result<?> configProxy(UserInfo ui, @RequestParam Map<String, Object> param) {
        logger.warn("configProxy:{}, param:{}", param, ui);
        // 配置
        Result<?> configResult = mqDeployer.configProxy(param);
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
    public Result<?> startup(UserInfo ui, @RequestParam(name="ip") String ip,
                             @RequestParam(name="dir") String dir,
                             @RequestParam(name = "listenPort") int port) {
        logger.warn("startup, ip:{}, dir:{}, user:{}", ip, dir, ui);
        return mqDeployer.startup(ip, dir, port);
    }

    /**
     * 启动新的broker
     * @return
     */
    @RequestMapping(value="/startup/new/broker", method=RequestMethod.POST)
    public Result<?> startupNewBroker(UserInfo ui, @RequestParam(name="ip") String ip,
                             @RequestParam(name="dir") String dir,
                             @RequestParam(name = "listenPort") int port,
                             @RequestParam(name = "cid", defaultValue = "0") int cid) {
        logger.warn("startup new broker, ip:{}, dir:{}, user:{}", ip, dir, ui);
        Result<?> result = mqDeployer.startup(ip, dir, port);
        if (cid != 0 && result.isOK()) {
            brokerService.deleteBrokerTmp(cid, ip + ":" + port);
        }
        return result;
    }
    
    /**
     * 启动，校验port方式重复启动
     * 
     * @return
     */
    @RequestMapping(value = "/startup/broker", method = RequestMethod.POST)
    public Result<?> startupBroker(UserInfo ui, @RequestParam(name = "ipAddr") String ipAddr,
            @RequestParam(name = "dir") String dir,
            @RequestParam(name = "cid", defaultValue = "0") int cid) {
        logger.warn("startup broker, ipAddr:{}, dir:{}, user:{}", ipAddr, dir, ui);
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
                    result.setMessage(result.getResult().toString() + "端口被占用，请确定broker是否已经启动");
                }
            }
            return result;
        }
        // 启动
        Result<?> startupResult = mqDeployer.startup(ip, dir, port);
        if (cid != 0 && startupResult.isOK()) {
            brokerService.deleteBrokerTmp(cid, ip + ":" + port);
        }
        return startupResult;
    }

    @RequestMapping(value = "/_shutdown", method = RequestMethod.POST)
    public Result<?> _shutdown(UserInfo ui, @RequestParam(name = "cid") int cid, @RequestParam(name = "addr") String addr) {
        return shutdown(ui, cid, addr);
    }

    /**
     * 下线
     * @param cid
     * @param broker
     * @return
     */
    @RequestMapping(value="/shutdown", method=RequestMethod.POST)
    public Result<?> shutdown(UserInfo ui, @RequestParam(name = "cid") int cid, @RequestParam(name = "addr") String addr) {
        logger.warn("shutdown:{}, user:{}", addr, ui);
        String[] addrs = addr.split(":");
        String ip = addrs[0];
        String portStr = addrs[1];
        int port = NumberUtils.toInt(portStr);
        if (port == 0) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Result<Broker> brokerResult = brokerService.queryBroker(cid, addr);
        if (brokerResult.isNotOK()) {
            return brokerResult;
        }
        Result<?> shutdownResult = mqDeployer.shutdown(ip, port, brokerResult.getResult().getBaseDir());
        if (shutdownResult.isOK()) {
            // 关闭后的broker更新状态
            brokerService.updateWritable(cid, addr, true);
        }
        return shutdownResult;
    }

    /**
     * 备份数据
     * @param ip
     * @param dir
     * @return
     */
    @RequestMapping(value="/backup", method=RequestMethod.POST)
    public Result<?> backup(UserInfo ui, @RequestParam(name = "ip") String ip, @RequestParam(name="dir") String dir) {
        logger.warn("backup, ip:{}, dir:{}, user:{}", ip, dir, ui);
        Result<?> mvResult = mqDeployer.backup(ip, dir);
        if(mvResult.isNotOK()) {
            return mvResult;
        }
        return Result.getOKResult();
    }

    /**
     * 恢复数据
     * @param ip
     * @param dir
     * @return
     */
    @RequestMapping(value="/recover", method=RequestMethod.POST)
    public Result<?> recover(UserInfo ui, @RequestParam(name="ip") String ip,
                             @RequestParam(name="dir") String dir) {
        logger.warn("recover, ip:{}, dir:{}, user:{}", ip, dir, ui);
        // 恢复数据
        Result<?> mvResult = mqDeployer.recover(ip, dir);
        if(mvResult.isNotOK()) {
            return mvResult;
        }
        return Result.getOKResult();
    }
    
    /**
     * 机器互信
     * @param sourceIp
     * @param destIp
     * @return
     */
    @RequestMapping("/authentication")
    public Result<?> authentication(@RequestParam(name = "sourceIp") String sourceIp,
            @RequestParam(name = "destIp") String destIp) {
        return mqDeployer.authentication(sourceIp, destIp);
    }
    
    /**
     * 获取存储文件
     * @param ip
     * @param home
     * @param map
     * @return
     */
    @RequestMapping("/store/file")
    public ModelAndView getSotreFileList(@RequestParam(name = "sourceIp") String ip,
            @RequestParam(name = "sourceHome") String home, Map<String, Object> map) {
        setResult(map, mqDeployer.getStoreFileList(ip, home));
        return new ModelAndView("admin/broker/storeFile");
    }
    
    /**
     * 创建存储路径
     * @param destIp
     * @param destHome
     * @return
     */
    @RequestMapping("/create/store/path")
    public Result<?> createStorePath(@RequestParam(name = "destIp") String destIp,
            @RequestParam(name = "destHome") String destHome) {
        return mqDeployer.createStorePath(destIp, destHome);
    }
    
    /**
     * scp传输文件
     * @param storeFileParam
     * @return
     */
    @RequestMapping("/scp/storefile")
    public Result<?> scpStoreFile(@RequestBody StoreFileParam storeFileParam) {
        return mqDeployer.scpStoreEntry(storeFileParam.getSourceIp(), storeFileParam.getSourceHome(),
                storeFileParam.getDestIp(), storeFileParam.getDestHome(), storeFileParam.getStoreFile());
    }

    /**
     * 删除部署未成功的Broker
     */
    @PostMapping("/delete/tmp/broker")
    public Result<?> deleteTmpBroker(UserInfo ui, @RequestParam(name = "addr") String addr,
                                     @RequestParam(name = "cid") int cid) {
        logger.warn("deleteTmpBroker, addr:{}, cid:{}, user:{}", addr, cid, ui);
        Result<Broker> brokerResult = brokerService.queryTmpBroker(cid, addr);
        if (brokerResult.isNotOK()) {
            return Result.getWebResult(brokerResult);
        }
        Broker broker = brokerResult.getResult();
        Result<?> deleteResult = mqDeployer.delete(addr.split(":")[0], broker.getBaseDir());
        if (deleteResult.isOK()) {
            return brokerService.deleteBrokerTmp(cid, addr);
        }
        return deleteResult;
    }

    @Override
    public String viewModule() {
        return "deploy";
    }
}
