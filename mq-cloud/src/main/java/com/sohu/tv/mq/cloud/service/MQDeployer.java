package com.sohu.tv.mq.cloud.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sohu.tv.mq.util.JSONUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.bo.BrokerConfig;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.StoreFiles;
import com.sohu.tv.mq.cloud.bo.StoreFiles.StoreFile;
import com.sohu.tv.mq.cloud.bo.StoreFiles.StoreFileType;
import com.sohu.tv.mq.cloud.bo.SubscriptionGroup;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.SSHTemplate.DefaultLineProcessor;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHCallback;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHSession;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.SSHException;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.ScpDirVO;
import com.sohu.tv.mq.cloud.web.vo.ScpVO;

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
    
    public static final String MQ_CLOUD_CONFIG_INIT_FLAG = "/home/mqcloud/.mq_cloud_inited";
    
    public static final String MQ_CLOUD_OS_SH = "%s/bin/os.sh";
    
    public static final String TMP_DIR = "/tmp/";
    
    public static final String PID = "`sudo netstat -npl | grep \":%s\" | awk '{print $NF}' | awk -F\"/java\" '{print $1}'`";
    
    public static final String RUN_FILE = "run.sh";
    
    public static final String CONFIG_FILE = "mq.conf";
    
    public static final String RUN_CONFIG = "echo \"source /etc/profile;nohup sh %s/bin/%s -c %s/" + CONFIG_FILE 
            + " >> %s/logs/startup.log 2>&1 &\" > %s/" + RUN_FILE;
    
    public static final String DATA_LOGS_DIR = "mkdir -p %s/data/config|mkdir -p %s/logs|";

    public static final String JVM_OPTION_JVMMEMORY = "sed -i 's|-server -Xms8g -Xmx8g|-server -Xms%s -Xmx%s|g' %s/bin/runbroker.sh";

    public static final String JVM_OPTION_DIRECTMEMORY = "sed -i 's|-XX:MaxDirectMemorySize=15g|-XX:MaxDirectMemorySize=%s|g' %s/bin/runbroker.sh";

    public static final String MQ_AUTH = "/tmp/mqauth";

    // 部署broker时自动创建监控订阅组
    public static final String SUBSCRIPTIONGROUP_JSON = "echo '"
            + JSONUtil.toJSONString(SubscriptionGroup.buildMonitorSubscriptionGroup())
            + "' > %s/data/config/subscriptionGroup.json";
   
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private SSHTemplate sshTemplate;
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private RocketMQFileService rocketMQFileService;
    
    @Autowired
    private BrokerConfigService brokerConfigService;
    
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
        String comm = "if [ ! -d \"" + dir + "\" ];then sudo mkdir -p " + dir + " && sudo chown mqcloud:mqcloud " + dir
                + ";else echo 0;fi";
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(comm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("isNotUsed, ip:{},dir:{}", ip, dir, e);
            return Result.getWebErrorResult(e);
        }
        Result<?> result = wrapSSHResult(sshResult);
        
        if(result.isOK() && "0".equals(result.getResult())) {
            return Result.getResult(Status.DB_ERROR).setMessage("目录已存在");
        }
        return result;
    }
    
    /**
     * 上传rocketmq.zip文件
     * @param ip
     * @return
     */
    public Result<?> scp(String ip){
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    byte[] rocketmqFile = rocketMQFileService.getRocketmqFile();
                    SSHResult sshResult = session.scpToDir(rocketmqFile, MQCloudConfigHelper.ROCKETMQ_FILE, TMP_DIR);
                    rocketmqFile = null;
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("scp:{}, ", ip, e);
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
        return unzip(ip, dest, TMP_DIR + MQCloudConfigHelper.ROCKETMQ_FILE);
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
    public Result<?> configNameServer(String ip, int port, String absoluteDir){
        String absoluteConfig = absoluteDir + "/" + CONFIG_FILE;
        String mqConf = "echo \"kvConfigPath="+absoluteDir+"/data/kvConfig.json\" >> " + absoluteConfig + "|"
                + "echo \"listenPort="+port+"\" >> " + absoluteConfig + "|";
        if(mqCloudConfigHelper.isAdminAclEnable()) {
            mqConf += "echo \"adminAclEnable=true\" >> " + absoluteConfig + "|";
        }
        String comm = String.format(DATA_LOGS_DIR, absoluteDir, absoluteDir)
                + mqConf
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
    public Result<?> configBroker(Map<String, Object> param){
        String clusterName = param.get("brokerClusterName").toString();
        Cluster cluster = clusterService.getMQClusterByName(clusterName);
        String absoluteDir = param.get("dir").toString();
        String absoluteConfig = absoluteDir + "/" + CONFIG_FILE;
        String brokerIp = param.get("ip").toString();
        // 1.基础配置
        String comm = String.format(DATA_LOGS_DIR, absoluteDir, absoluteDir)
                + "mkdir -p " + param.get("storePathRootDir") + "/consumequeue " + param.get("storePathCommitLog")
                + "|echo -e \""
                + map2String(param, cluster.getId())
                + "\" > " + absoluteConfig + "|"
                + String.format(RUN_CONFIG, absoluteDir, "mqbroker", absoluteDir, absoluteDir, absoluteDir);
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(brokerIp, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(comm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("configBroker, ip:{},comm:{}", brokerIp, comm, e);
            return Result.getWebErrorResult(e);
        }
        Result<?> configResult = wrapSSHResult(sshResult);
        if(configResult.isNotOK()) {
            return configResult;
        }
        // 2.启动配置，替换启动参数
        StringBuilder optionCommond = new StringBuilder();
        Optional.ofNullable(param.get("jvmMemory")).ifPresent(xmx -> {
            optionCommond.append(String.format(JVM_OPTION_JVMMEMORY, xmx, xmx, absoluteDir)).append(";");
        });
        Optional.ofNullable(param.get("maxDirectMemorySize")).ifPresent(maxDirectMemorySize -> {
            optionCommond.append(String.format(JVM_OPTION_DIRECTMEMORY, maxDirectMemorySize, absoluteDir));
        });
        if (optionCommond.length() > 0) {
            SSHResult sedResult = null;
            try {
                sedResult = sshTemplate.execute(brokerIp, new SSHCallback() {
                    public SSHResult call(SSHSession session) {
                        SSHResult sshResult = session.executeCommand(optionCommond.toString());
                        return sshResult;
                    }
                });
            } catch (SSHException e) {
                logger.error("configBroker, ip:{},comm:{}", brokerIp, comm, e);
                return Result.getWebErrorResult(e);
            }
            Result<?> configSedResult = wrapSSHResult(sedResult);
            if (configSedResult.isNotOK()) {
                return configSedResult;
            }
        }
        // 3.初始化监控订阅信息
        final String subscriptionGroupComm = String.format(SUBSCRIPTIONGROUP_JSON, absoluteDir);
        try {
            sshResult = sshTemplate.execute(brokerIp, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(subscriptionGroupComm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("init subscriptionGroup, ip:{},comm:{}", brokerIp, subscriptionGroupComm, e);
            return Result.getWebErrorResult(e);
        }
        configResult = wrapSSHResult(sshResult);
        if(configResult.isNotOK()) {
            return configResult;
        }
        
        // slave直接返回
        if("SLAVE".equals(param.get("brokerRole"))) {
            return Result.getOKResult();
        }
        // 获取master地址
        Result<String> masterAddressResult = fetchMasterAddress(cluster);
        // 集群中无节点时，不进行后续配置
        if(Status.NO_RESULT.getKey() == masterAddressResult.getStatus()) {
            return Result.getOKResult();
        }
        if(!masterAddressResult.isOK()) {
            return masterAddressResult;
        }
        String masterAddress = masterAddressResult.getResult();
        // 4.1抓取topic配置
        Result<String> result = fetchTopicConfig(cluster, masterAddress);
        if(Status.DB_ERROR.getKey() == result.getStatus()) {
            return result;
        }
        
        // 4.2保存topic配置
        Result<?> topicSSHResult = saveConfig(brokerIp, result.getResult(), absoluteDir, "topics.json");
        if(!topicSSHResult.isOK()) {
            return topicSSHResult;
        }
        
        // 5.1抓取consumer配置
        Result<String> consumerResult = fetchConsumerConfig(cluster, masterAddress);
        if(Status.DB_ERROR.getKey() == consumerResult.getStatus()) {
            return consumerResult;
        }
        
        // 5.2保存consumer配置
        Result<?> consumerSSHResult = saveConfig(brokerIp, consumerResult.getResult(), absoluteDir, 
                "subscriptionGroup.json");
        return consumerSSHResult;
    }
    
    private String map2String(Map<String, Object> param, int cid) {
        StringBuilder sb = new StringBuilder();
        Result<List<BrokerConfig>> result = brokerConfigService.queryByCid(cid);
        for (String key : param.keySet()) {
            for (BrokerConfig brokerConfig : result.getResult()) {
                if (brokerConfig.getKey().equals(key)) {
                    sb.append(key + "=" + param.get(key) + "\n");
                }
            }
        }
        return sb.toString();
    }
    
    private Result<?> saveConfig(String ip, String content, String absoluteDir, String fileName) {
        SSHResult sshResult = null;
        try {
            // save config to /tmp
            MixAll.string2File(content, "/tmp/" + fileName);
            
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.scpToDir("/tmp/" + fileName, absoluteDir+"/data/config/");
                    return sshResult;
                }
            });
        } catch (Exception e) {
            logger.error("configBroker {}, ip:{}, content:{}", fileName, ip, content, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
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
     * 获取master地址
     * @param brokerRole
     * @return
     */
    private Result<String> fetchMasterAddress(Cluster cluster){
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
                return Result.getResult(masterAddr);
            }

            public Result<String> exception(Exception e) throws Exception {
                logger.error("fetchMasterAddress:{} error", cluster, e);
                return Result.getDBErrorResult(e);
            }
            public Cluster mqCluster() {
                return cluster;
            }
        });
    }
    
    /**
     * 获取topic的配置
     * @param brokerRole
     * @return
     */
    private Result<String> fetchTopicConfig(Cluster cluster, String masterAddress){
        // 获取topic配置
        return mqAdminTemplate.execute(new MQAdminCallback<Result<String>>() {
            public Result<String> callback(MQAdminExt mqAdmin) throws Exception {
                // 获取topic配置
                TopicConfigSerializeWrapper topicWrapper = mqAdmin.getAllTopicGroup(masterAddress, 10 * 1000);
                if(topicWrapper == null) {
                    return Result.getResult(Status.NO_RESULT);
                }
                return Result.getResult(JSONUtil.toJSONString(topicWrapper));
            }

            public Result<String> exception(Exception e) throws Exception {
                logger.error("fetchTopicConfig:{} error", masterAddress, e);
                return Result.getDBErrorResult(e);
            }

            public Cluster mqCluster() {
                return cluster;
            }
        });
    }
    
    /**
     * 获取consumer的配置
     * @param brokerRole
     * @return
     */
    private Result<String> fetchConsumerConfig(Cluster cluster, String masterAddress){
        // 获取topic配置
        return mqAdminTemplate.execute(new MQAdminCallback<Result<String>>() {
            public Result<String> callback(MQAdminExt mqAdmin) throws Exception {
                // 获取topic配置
                SubscriptionGroupWrapper subscriptionWrapper = mqAdmin.getAllSubscriptionGroup(masterAddress, 10 * 1000);
                if(subscriptionWrapper == null) {
                    return Result.getResult(Status.NO_RESULT);
                }
                return Result.getResult(JSONUtil.toJSONString(subscriptionWrapper));
            }

            public Result<String> exception(Exception e) throws Exception {
                logger.error("fetchConsumerConfig:{} error", masterAddress, e);
                return Result.getDBErrorResult(e);
            }

            public Cluster mqCluster() {
                return cluster;
            }
        });
    }

    /**
     * 备份数据
     * @param ip
     * @param sourceDir
     * @param destDir
     * @return
     */
    public Result<?> backup(String ip, String sourceDir, String destDir) {
        String comm = "sudo mv " + sourceDir + " " + destDir;
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(comm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("backup err, ip:{},sourceDir:{},destDir:{},comm:{}", ip, sourceDir, destDir, comm, e);
            return Result.getWebErrorResult(e);
        }
        Result<?> result = wrapSSHResult(sshResult);
        // 创建新目录
        if (result.isOK()) {
            dirWrite(ip, sourceDir);
        }
        return result;
    }

    /**
     * 移动备份数据到新安装目录
     * @param ip
     * @param backupDir
     * @param destDir
     * @return
     */
    public Result<?> recover(String ip, String backupDir, String destDir) {
        String mvCommTemplate = "sudo mv %s/%s %s/";
        // 1. 移动mq.conf
        String mvConfig = String.format(mvCommTemplate, backupDir, CONFIG_FILE, destDir);
        // 2. 移动run.sh
        String mvRun = String.format(mvCommTemplate, backupDir, RUN_FILE, destDir);
        // 3. 移动data目录
        String mvData = String.format(mvCommTemplate, backupDir, "data", destDir);
        // 4. 创建logs目录
        String createLogsDir = String.format("mkdir -p %s/logs", destDir);
        // 顺序执行,各个命令之间没有依赖
        String comm = new StringBuilder()
                .append(mvConfig).append(" ; ")
                .append(mvRun).append(" ; ")
                .append(mvData).append(" ; ")
                .append(createLogsDir).toString();
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(comm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("recover err, ip:{},backupDir:{},destDir:{},comm:{}", ip, backupDir, destDir, comm, e);
            return Result.getWebErrorResult(e);
        }
        // 检测执行结果
        Result<?> mvResult = wrapSSHResult(sshResult);
        if (mvResult.isNotOK()) {
            return mvResult;
        }
        // 5. 备份目录重命名
        String renameBackupDirComm = "sudo mv " + backupDir + " " + backupDir + DateUtil.getFormatNow(DateUtil.YMDHMS);
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(renameBackupDirComm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("renameBackupDir err, ip:{},comm:{}", ip, renameBackupDirComm, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * startup
     * @param ip
     * @return
     */
    public Result<?> startup(String ip, String absoluteDir){
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
     * 机器互信
     * @param sourceIp
     * @param destIp
     * @return
     */
    public Result<?> authentication(String sourceIp, String destIp) {
        // 1.生产无密互信公私钥
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(sourceIp, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    return session.executeCommand("[ ! -e '" + MQ_AUTH + "' ] && ssh-keygen -t rsa -f " + MQ_AUTH + " -N '' -q");
                }
            });
        } catch (SSHException e) {
            logger.error("ssh-keygen, sourceIp:{}", sourceIp, e);
            return Result.getWebErrorResult(e);
        }
        if (!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        // 2.读取公钥
        sshResult = null;
        try {
            sshResult = sshTemplate.execute(sourceIp, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    return session.executeCommand("cat " + MQ_AUTH + ".pub");
                }
            });
        } catch (SSHException e) {
            logger.error("cat " + MQ_AUTH + ".pub, sourceIp:{}", sourceIp, e);
            return Result.getWebErrorResult(e);
        }
        if (!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        String pubKey = sshResult.getResult();
        // 3.写入目标机器
        sshResult = null;
        try {
            sshResult = sshTemplate.execute(destIp, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    return session.executeCommand("[ ! -e '/home/mqcloud/.ssh' ] && mkdir -p -m 700 /home/mqcloud/.ssh;"
                            + "[ ! -e '/home/mqcloud/.ssh/authorized_keys' ] && touch /home/mqcloud/.ssh/authorized_keys && chmod 600 /home/mqcloud/.ssh/authorized_keys;"
                            + "echo '" + pubKey + "' >> /home/mqcloud/.ssh/authorized_keys;");
                }
            });
        } catch (SSHException e) {
            logger.error("authorized_keys, sourceIp:{}, destIp:{}", sourceIp, destIp, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * 获取存储文件
     * @param ip
     * @param home
     * @return
     */
    public Result<?> getStoreFileList(String ip, String home) {
        String absoluteDir = home + "/data";
        SSHResult sshResult = null;
        StoreFiles storeFiles = new StoreFiles();
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    return session.executeCommand("cd " + absoluteDir + ";find -type f | xargs du -b",
                            new DefaultLineProcessor() {
                                public void process(String line, int lineNum) throws Exception {
                                    String[] tmpArray = line.split("\\s+");
                                    if (tmpArray[1].startsWith(".")) {
                                        tmpArray[1] = tmpArray[1].substring(1);
                                    }
                                    storeFiles.addStoreFile(tmpArray[1], NumberUtils.toLong(tmpArray[0]));
                                }
                            }, 60 * 1000);
                }
            });
        } catch (SSHException e) {
            logger.error("startup, ip:{},home:{}", ip, absoluteDir, e);
            return Result.getWebErrorResult(e);
        }
        if (!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        if (storeFiles.getStoreEntryMap().size() == 0) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(ip + " " + absoluteDir + " 无数据");
        }
        storeFiles.sort();
        return Result.getResult(storeFiles);
    }
    
    /**
     * 创建存储路径
     * @param ip
     * @param home
     * @return
     */
    public Result<?> createStorePath(String ip, String home) {
        String absoluteDir = home + "/data";
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    StringBuilder comm = new StringBuilder("mkdir -p ");
                    for(StoreFileType storeFileType : StoreFileType.values()) {
                        comm.append(absoluteDir);
                        comm.append(storeFileType.getPath());
                        comm.append(" ");
                    }
                    return session.executeCommand(comm.toString());
                }
            });
        } catch (SSHException e) {
            logger.error("startup, ip:{},home:{}", ip, absoluteDir, e);
            return Result.getWebErrorResult(e);
        }
        return wrapSSHResult(sshResult);
    }
    
    /**
     * scp存储条目
     * 
     * @param ip
     * @param home
     * @return
     */
    public Result<?> scpStoreEntry(String sourceIp, String sourceHome, String destIp, String destHome,
            StoreFile storeFile) {
        if (storeFile.getSubEntryListSize() > 1) {
            return scpStoreFolder(sourceIp, sourceHome, destIp, destHome, storeFile);
        } else {
            return scpStoreFile(sourceIp, sourceHome, destIp, destHome, storeFile);
        }
    }
    
    /**
     * scp存储文件
     * @param ip
     * @param home
     * @return
     */
    public Result<?> scpStoreFile(String sourceIp, String sourceHome, String destIp, String destHome,
            StoreFile storeFile) {
        long start = System.currentTimeMillis();
        // 复制文件
        String absoluteStorePath = storeFile.toAbsoluteStorePath();
        String sourceFile = sourceHome + "/data" + absoluteStorePath;
        String destFile = destHome + "/data" + absoluteStorePath;
        SSHResult sshResult = null;
        // 先创建需要的存储目录
        if (storeFile.getParentName() != null) {
            try {
                sshResult = sshTemplate.execute(destIp, new SSHCallback() {
                    public SSHResult call(SSHSession session) {
                        return session.executeCommand("mkdir -p " + destFile.substring(0, destFile.lastIndexOf("/")));
                    }
                });
            } catch (SSHException e) {
                logger.error("mkdir destIp:{}, destHome:{}", destIp, destHome, e);
                return Result.getWebErrorResult(e);
            }
            if (!sshResult.isSuccess()) {
                return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
            }
        }
        // 复制文件
        try {
            sshResult = sshTemplate.execute(sourceIp, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    return session.executeCommand(
                            "scp -o BatchMode=yes -o StrictHostKeyChecking=no -i " + MQ_AUTH + " -pq -l 819200 " + sourceFile 
                            + " mqcloud@" + destIp + ":" + destFile,
                            30 * 60 * 1000);
                }
            });
        } catch (SSHException e) {
            logger.error("scp, ip:{}, sourceHome:{}, destIp:{}, destHome:{}", sourceIp, sourceHome, destIp, destHome, e);
            return Result.getWebErrorResult(e);
        }
        if (!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        // 源md5
        try {
            sshResult = sshTemplate.execute(sourceIp, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    return session.executeCommand("md5sum " + sourceFile, 60 * 1000);
                }
            });
        } catch (SSHException e) {
            logger.error("mkdir destIp:{}, destHome:{}", destIp, destHome, e);
            return Result.getWebErrorResult(e);
        }
        if (!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        String sourceMD5 = sshResult.getResult().split("\\s+")[0];
        // 目标md5
        try {
            sshResult = sshTemplate.execute(destIp, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    return session.executeCommand("md5sum " + destFile, 60 * 1000);
                }
            });
        } catch (SSHException e) {
            logger.error("mkdir destIp:{}, destHome:{}", destIp, destHome, e);
            return Result.getWebErrorResult(e);
        }
        if (!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        String destMD5 = sshResult.getResult().split("\\s+")[0];
        // md5不一致需要校验大小是否一致
        if (!sourceMD5.equals(destMD5)) {
            // 目标大小
            try {
                sshResult = sshTemplate.execute(destIp, new SSHCallback() {
                    public SSHResult call(SSHSession session) {
                        return session.executeCommand("du -b " + destFile);
                    }
                });
            } catch (SSHException e) {
                logger.error("mkdir destIp:{}, destHome:{}", destIp, destHome, e);
                return Result.getWebErrorResult(e);
            }
        }
        if (!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        String[] tmpArray = sshResult.getResult().split("\\s+");
        long destSize = NumberUtils.toLong(tmpArray[0]);
        // 结果封装
        ScpVO scpVO = new ScpVO(sourceMD5, destMD5, System.currentTimeMillis() - start, storeFile.getSize(), destSize);
        return Result.getResult(scpVO);
    }
    
    /**
     * scp存储路径
     * @param ip
     * @param home
     * @return
     */
    public Result<?> scpStoreFolder(String sourceIp, String sourceHome, String destIp, String destHome,
            StoreFile storeFile) {
        long start = System.currentTimeMillis();
        StoreFileType storeFileType = StoreFileType.findStoreFileType(storeFile.getType());
        String sourceDataDir = sourceHome + "/data" + storeFileType.getPath();
        String destDataDir = destHome + "/data" + storeFileType.getPath();
        SSHResult sshResult = null;
        // 复制目录
        try {
            sshResult = sshTemplate.execute(sourceIp, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    return session.executeCommand("cd " + sourceDataDir + ";tar cz " + storeFile.getName() + "|ssh -i " 
                          + MQ_AUTH + " -q mqcloud@" + destIp + " \"tar xzm -C " + destDataDir + "\"", 30 * 60 * 1000);
                }
            });
        } catch (SSHException e) {
            logger.error("scp, ip:{}, sourceHome:{}, destIp:{}, destHome:{}", sourceIp, sourceHome, destIp, destHome, e);
            return Result.getWebErrorResult(e);
        }
        if (!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        // 源md5
        Map<String, ScpVO> scpVOMap = new HashMap<>();
        sshResult = getMD5(scpVOMap, sourceIp, sourceDataDir + "/" + storeFile.getName(), true);
        if (!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        // 目标md5
        sshResult = getMD5(scpVOMap, destIp, destDataDir + "/" + storeFile.getName(), false);
        if (!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        // md5不一致需要校验大小是否一致
        boolean md5Equal = true;
        for (ScpVO scpVO : scpVOMap.values()) {
            if (!scpVO.isMD5OK()) {
                md5Equal = false;
                break;
            }
        }
        boolean sizeEqual = true;
        if (!md5Equal) {
            // 源大小
            sshResult = getSize(scpVOMap, sourceIp, sourceDataDir + "/" + storeFile.getName(), true);
            if (!sshResult.isSuccess()) {
                return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
            }
            // 目标大小
            sshResult = getSize(scpVOMap, destIp, destDataDir + "/" + storeFile.getName(), false);
            if (!sshResult.isSuccess()) {
                return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
            }
            for (ScpVO scpVO : scpVOMap.values()) {
                if (!scpVO.isSizeOK()) {
                    sizeEqual = false;
                    break;
                }
            }
        }
        // 结果封装
        ScpDirVO scpDirVO = new ScpDirVO(md5Equal, sizeEqual, System.currentTimeMillis() - start, storeFile.getSize(), scpVOMap);
        return Result.getResult(scpDirVO);
    }
    
    /**
     * 获取md5
     * @param scpVOMap
     * @param ip
     * @param path
     * @param source
     * @return
     */
    private SSHResult getMD5(Map<String, ScpVO> scpVOMap, String ip, String path, boolean source) {
        try {
            return sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    return session.executeCommand("cd " + path + ";find -type f | xargs md5sum",
                            new DefaultLineProcessor() {
                                public void process(String line, int lineNum) throws Exception {
                                    String[] tmpArray = line.split("\\s+");
                                    if (tmpArray[1].startsWith(".")) {
                                        tmpArray[1] = tmpArray[1].substring(1);
                                    }
                                    if(source) {
                                        ScpVO scpVO = new ScpVO(tmpArray[0], null, 0, 0, 0);
                                        scpVOMap.put(tmpArray[1], scpVO);
                                    } else {
                                        ScpVO scpVO = scpVOMap.get(tmpArray[1]);
                                        if (scpVO != null) {
                                            scpVO.setDestMD5(tmpArray[0]);
                                        }
                                    }
                                }
                            });
                }
            });
        } catch (SSHException e) {
            logger.error("md5 ip:{}, path:{}", ip, path, e);
            return sshTemplate.new SSHResult(e);
        }
    }
    
    /**
     * 获取md5
     * @param scpVOMap
     * @param ip
     * @param path
     * @param source
     * @return
     */
    private SSHResult getSize(Map<String, ScpVO> scpVOMap, String ip, String path, boolean source) {
        try {
            return sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    return session.executeCommand("cd " + path + ";find -type f | xargs du -b",
                            new DefaultLineProcessor() {
                                public void process(String line, int lineNum) throws Exception {
                                    String[] tmpArray = line.split("\\s+");
                                    if (tmpArray[1].startsWith(".")) {
                                        tmpArray[1] = tmpArray[1].substring(1);
                                    }
                                    ScpVO scpVO = scpVOMap.get(tmpArray[1]);
                                    if (scpVO != null) {
                                        if(source) {
                                            scpVO.setSourceSize(NumberUtils.toLong(tmpArray[0]));
                                        } else {
                                            scpVO.setDestSize(NumberUtils.toLong(tmpArray[0]));
                                        }
                                    }
                                }
                            });
                }
            });
        } catch (SSHException e) {
            logger.error("du ip:{}, path:{}", ip, path, e);
            return sshTemplate.new SSHResult(e);
        }
    }
    
    /**
     * 判断目录是否存在
     * @param ip
     * @return
     */
    public Result<?> dirExist(String ip, String dir){
        String destDir = dir + "/data";
        String comm = "if [ -d \"" +destDir+ "\" ];then echo 1;else echo 0;fi";
        SSHResult sshResult = null;
        try {
            sshResult = sshTemplate.execute(ip, new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    SSHResult sshResult = session.executeCommand(comm);
                    return sshResult;
                }
            });
        } catch (SSHException e) {
            logger.error("dirExist, ip:{},dir:{}", ip, destDir, e);
            return Result.getWebErrorResult(e);
        }
        Result<?> result = wrapSSHResult(sshResult);
        if(result.isOK() && "0".equals(result.getResult())) {
            return Result.getResult(Status.DB_ERROR).setMessage("目录不存在");
        }
        return result;
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
        if(sshResult.getExcetion() != null) {
            return Result.getWebErrorResult(sshResult.getExcetion());
        }
        if(!sshResult.isSuccess()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage(sshResult.getResult());
        }
        if(sshResult.isSuccess() && sshResult.getResult() != null) {
            return Result.getResult(sshResult.getResult());
        }
        return Result.getOKResult();
    }
}
