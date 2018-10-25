package com.sohu.tv.mq.cloud.service;

import java.util.HashMap;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHCallback;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHSession;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.SSHException;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.BrokerParam;

/**
 * MQ部署
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月15日
 */
@Component
public class MQDeployer {
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    public static final String NS_SUB_GROUP = "nsaddr-%s";
    
    public static final String MQ_CLOUD_DIR = "/opt/mqcloud/";
    
    public static final String MQ_CLOUD_CONFIG_INIT_FLAG = MQ_CLOUD_DIR + ".mq_cloud_inited";
    
    public static final String MQ_CLOUD_OS_SH = MQ_CLOUD_DIR + "%s/bin/os.sh";
    
    public static final String TMP_DIR = "/tmp/";
    
    public static final String PID = "`sudo netstat -npl | grep \":%s\" | awk '{print $NF}' | awk -F\"/java\" '{print $1}'`";
    
    public static final String RUN_FILE = "run.sh";
    
    public static final String CONFIG_FILE = "mq.conf";
    
    public static final String RUN_CONFIG = "echo \"source /etc/profile;nohup sh %s/bin/%s -c %s/" + CONFIG_FILE 
            + " >> %s/logs/startup.log 2>&1 &\" >> %s/" + RUN_FILE;
    
    public static final String DATA_LOGS_DIR = "mkdir -p %s/data/config|mkdir -p %s/logs|";
   
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private SSHTemplate sshTemplate;
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Autowired
    private ClusterService clusterService;
    
    /**
     * 获取jdk版本
     * @param ip
     * @return
     */
    public Result<?> getJDKVersion(String ip){
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand("source /etc/profile;javap -version");
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("getJDKVersion:{}", ip, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * 获取jdk版本
     * @param ip
     * @return
     */
    public Result<?> getProgram(String ip, int port){
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand("sudo ps -fp " + String.format(PID, port));
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("getProgram, ip:{},port{}", ip, port, e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * 获取监听某个端口的信息
     * @param ip
     * @return
     */
    public Result<?> getListenPortInfo(String ip, int port){
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand("sudo netstat -npl | grep -w " + port);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("getListenPortInfo, ip:{},port:{}", ip, port, e);
            return Result.getWebErrorResult(e);
        }
        Result<?> result = wrapSSHResult(sshResult);
        
        // 端口被占用
        if(result.isOK() && result.getResult() != null) {
            result.setStatus(Status.DB_ERROR.getKey());
        }
        return result;
    }
    
    /**
     * 判断目录是否被占用
     * @param ip
     * @return
     */
    public Result<?> dirWrite(String ip, String dir){
        String destDir = MQ_CLOUD_DIR + dir;
        String comm = "if [ ! -d \"" +destDir+ "\" ];then mkdir -p " + destDir +";else echo 0;fi";
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(comm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("isNotUsed, ip:{},dir:{}", ip, destDir, e);
            return Result.getWebErrorResult(e);
        }
        Result<?> result = wrapSSHResult(sshResult);
        
        if(result.isOK() && "0".equals(result.getResult())) {
            return Result.getResult(Status.DB_ERROR).setMessage("目录已存在");
        }
        return result;
    }
    
    /**
     * wget
     * @param ip
     * @return
     */
    public Result<?> wget(String ip){
        return wget(ip, mqCloudConfigHelper.getRocketMQURL());
    }
    
    /**
     * wget
     * @param ip
     * @return
     */
    public Result<?> wget(String ip, String url){
        return wget(ip, TMP_DIR, url);
    }
    
    /**
     * wget
     * @param ip
     * @return
     */
    public Result<?> wget(String ip, String destDir, String url){
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand("wget -qP " + destDir + " \"" + url + "\"");
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("wget:{}, url:{}", ip, url, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * unzip
     * @param ip
     * @return
     */
    public Result<?> unzip(String ip, String dest){
        String destDir = MQ_CLOUD_DIR + dest;
        return unzip(ip, destDir, TMP_DIR + MQCloudConfigHelper.ROCKETMQ_FILE);
    }
    
    /**
     * unzip
     * @param ip
     * @return
     */
    public Result<?> unzip(String ip, String dest, String zipFile){
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand("unzip -d " + dest + " " + zipFile);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("unzip, ip:{},dest:{},zip:{}", ip, dest, zipFile, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * configNameServer
     * @param ip
     * @return
     */
    public Result<?> configNameServer(String ip, int port, String nsHome){
        String absoluteDir = MQ_CLOUD_DIR + nsHome;
        String absoluteConfig = absoluteDir + "/" + CONFIG_FILE;
        String comm = String.format(DATA_LOGS_DIR, absoluteDir, absoluteDir)
                + "echo \"kvConfigPath="+absoluteDir+"/data/kvConfig.json\" >> " + absoluteConfig + "|"
                + "echo \"listenPort="+port+"\" >> " + absoluteConfig + "|"
                + String.format(RUN_CONFIG, absoluteDir, "mqnamesrv", absoluteDir, absoluteDir, absoluteDir);
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(comm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("configNameServer, ip:{},comm:{}", ip, comm, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * configBroker
     * @param ip
     * @return
     */
    public Result<?> configBroker(BrokerParam brokerParam){
        String absoluteDir = MQ_CLOUD_DIR + brokerParam.getDir();
        String absoluteConfig = absoluteDir + "/" + CONFIG_FILE;
        Cluster cluster = clusterService.getMQClusterById(brokerParam.getMqClusterId());
        String comm = String.format(DATA_LOGS_DIR, absoluteDir, absoluteDir)
                + "echo -e \""+brokerParam.toConfig(mqCloudConfigHelper.getDomain(), cluster)
                + "\" > " + absoluteConfig + "|"
                + String.format(RUN_CONFIG, absoluteDir, "mqbroker", absoluteDir, absoluteDir, absoluteDir);
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(brokerParam.getIp(), new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(comm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("configBroker, ip:{},comm:{}", brokerParam.getIp(), comm, e);
            return Result.getWebErrorResult(e);
        }
        Result<?> configResult = wrapSSHResult(sshResult);
        if(configResult.isNotOK()) {
            return configResult;
        }
        // 抓取topic配置
        Result<String> result = fetchTopicConfig(brokerParam);
        if(Status.DB_ERROR.getKey() == result.getStatus()) {
            return result;
        }
        if(Status.NO_RESULT.getKey() == result.getStatus()) {
            return Result.getOKResult();
        }
        SSHResult topicSSHResult = null;
        try {
            // save config to /tmp
            MixAll.string2File(result.getResult(), "/tmp/topics.json");
            
            topicSSHResult = sshTemplate.execute(brokerParam.getIp(), new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.scpToDir("/tmp/topics.json", absoluteDir+"/data/config/");
                    return sshResult;
                }
            });
        } catch (Exception e) {
            logger.error("configBroker topic, ip:{},comm:{}", brokerParam.getIp(), result.getResult(), e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(topicSSHResult);
    }
    
    /**
     * 初始化配置
     * @param ip
     * @return
     */
    public Result<?> initConfig(String ip, String nsHome) {
        String comm = "if [ ! -f \"" + MQ_CLOUD_CONFIG_INIT_FLAG + "\" ];then sudo sh "
                + String.format(MQ_CLOUD_OS_SH, nsHome) + "|touch " + MQ_CLOUD_CONFIG_INIT_FLAG + ";fi";
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(comm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("initConfig, ip:{},comm:{}", ip, comm, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * 获取topic的配置
     * @param brokerRole
     * @return
     */
    private Result<String> fetchTopicConfig(BrokerParam brokerParam){
        if (brokerParam.isSlave()) {
            return Result.getResult(Status.NO_RESULT);
        }
        // 获取topic配置
        return mqAdminTemplate.execute(new MQAdminCallback<Result<String>>() {
            public Result<String> callback(MQAdminExt mqAdmin) throws Exception {
                // 获取集群配置
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                if(clusterInfo == null) {
                    return Result.getResult(Status.NO_RESULT);
                }
                HashMap<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
                if(brokerAddrTable == null || brokerAddrTable.size() == 0) {
                    return Result.getResult(Status.NO_RESULT);
                }
                BrokerData brokerData = brokerAddrTable.values().iterator().next();
                HashMap<Long, String> brokerAddrs = brokerData.getBrokerAddrs();
                if(brokerAddrs == null || brokerAddrs.size() == 0) {
                    return Result.getResult(Status.NO_RESULT);
                }
                String masterAddr = brokerAddrs.get(MixAll.MASTER_ID);
                if(masterAddr == null) {
                    return Result.getResult(Status.NO_RESULT);
                }
                // 获取topic配置
                TopicConfigSerializeWrapper topicWrapper = mqAdmin.getAllTopicGroup(masterAddr, 10 * 1000);
                if(topicWrapper == null) {
                    return Result.getResult(Status.NO_RESULT);
                }
                return Result.getResult(JSON.toJSONString(topicWrapper));
            }

            public Result<String> exception(Exception e) throws Exception {
                return Result.getDBErrorResult(e);
            }

            public Cluster mqCluster() {
                return clusterService.getMQClusterById(brokerParam.getMqClusterId());
            }
        });
    }
    
    /**
     * startup
     * @param ip
     * @return
     */
    public Result<?> startup(String ip, String home){
        String absoluteDir = MQ_CLOUD_DIR + home;
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand("sudo sh " + absoluteDir + "/" + RUN_FILE);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("startup, ip:{},home:{}", ip, absoluteDir, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * shutdown
     * @param ip
     * @return
     */
    public Result<?> shutdown(String ip, int port){
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand("sudo kill " + String.format(PID, port));
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("shutdown, ip:{},port:{}", ip, port, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * 包装返回结果
     * @param sshResult
     * @return
     */
    private Result<?> wrapSSHResult(SSHResult sshResult){
        if(sshResult == null) {
            return Result.getResult(Status.NO_RESULT);
        }
        if(!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        if(sshResult.getExcetion() != null) {
            return Result.getWebErrorResult(sshResult.getExcetion());
        }
        if(sshResult.isSuccess() && sshResult.getResult() != null) {
            return Result.getResult(sshResult.getResult());
        }
        return Result.getOKResult();
    }
}
