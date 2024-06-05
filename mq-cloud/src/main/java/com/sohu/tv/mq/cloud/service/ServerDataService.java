package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.dao.ServerAlarmConfigDao;
import com.sohu.tv.mq.cloud.dao.ServerStatusDao;
import com.sohu.tv.mq.cloud.service.SSHTemplate.DefaultLineProcessor;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHCallback;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHSession;
import com.sohu.tv.mq.cloud.task.server.data.OSInfo;
import com.sohu.tv.mq.cloud.task.server.data.Server;
import com.sohu.tv.mq.cloud.task.server.nmon.NMONService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.SSHException;
import com.sohu.tv.mq.cloud.util.Status;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;

/**
 * 服务器数据
 *
 * @author yongfeigao
 * @Description:
 * @date 2018年7月18日
 */
@Service
public class ServerDataService {
    private static final Logger logger = LoggerFactory.getLogger(ServerDataService.class);

    //获取监控结果
    public static final String COLLECT_SERVER_STATUS =
            "[ -e \"" + NMONService.SOCK_LOG + "\" ] && /bin/cat " + NMONService.SOCK_LOG + " >> " + NMONService.NMON_LOG
                    + ";[ -e \"" + NMONService.ULIMIT_LOG + "\" ] && /bin/cat " + NMONService.ULIMIT_LOG + " >> " + NMONService.NMON_LOG
                    + ";[ -e \"" + NMONService.DISK_LOG + "\" ] && /bin/cat " + NMONService.DISK_LOG + " >> " + NMONService.NMON_LOG
                    + ";[ -e \"" + NMONService.NMON_LOG + "\" ] && /bin/mv " + NMONService.NMON_LOG + " " + NMONService.NMON_OLD_LOG
                    + ";[ $? -eq 0 ] && /bin/cat " + NMONService.NMON_OLD_LOG;

    @Autowired
    private ServerStatusDao serverStatusDao;

    @Autowired
    private ServerAlarmConfigDao serverAlarmConfigDao;

    @Autowired
    private SSHTemplate sshTemplate;

    //nmon服务
    @Autowired
    private NMONService nmonService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private NameServerService nameServerService;

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private ControllerService controllerService;

    /**
     * 查询server信息
     *
     * @param ip
     * @return
     */
    public ServerInfo queryServerInfo(String ip) {
        try {
            return serverStatusDao.queryServerInfo(ip);
        } catch (Exception e) {
            logger.error("query err:{}", ip, e);
        }
        return null;
    }

    /**
     * 查询所有服务器
     *
     * @return List<ServerInfo>
     */
    public List<ServerInfo> queryAllServerInfo() {
        try {
            return serverStatusDao.queryAllServerInfo();
        } catch (Exception e) {
            logger.error("queryAllServerInfo err", e);
        }
        return Collections.emptyList();
    }

    /**
     * 查询今日当前所有服务器状态
     *
     * @return List<ServerInfoExt>
     */
    public List<ServerInfoExt> queryAllServer(Date date) {
        try {
            return serverStatusDao.queryAllServer(date);
        } catch (Exception e) {
            logger.error("queryAllServerStat err, date:{}", date, e);
        }
        return Collections.emptyList();
    }

    /**
     * 保存服务器发行版信息
     *
     * @param ip
     * @param dist
     * @param type
     */
    public Result<?> saveServerInfo(String ip, String dist, int type, String room) {
        if (dist == null) {
            return null;
        }
        dist = dist.trim();
        if (dist.length() == 0) {
            return null;
        }
        try {
            serverStatusDao.saveServerInfo(ip, dist, type, room);
        } catch (Exception e) {
            logger.error("saveServerInfo err:ip:{}, dist:{}, type:{}", ip, dist, type, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 保存/更新服务器信息
     *
     * @param server
     * @return
     */
    public Integer saveAndUpdateServerInfo(Server server) {
        if (server.getHost() == null || server.getNmon() == null || server.getCpus() == 0 ||
                server.getCpuModel() == null || server.getKernel() == null || server.getUlimit() == null) {
            return null;
        }
        try {
            return serverStatusDao.saveAndUpdateServerInfo(server);
        } catch (Exception e) {
            logger.error("saveAndUpdateServerInfo err server:{}", server, e);
        }
        return null;
    }

    /**
     * 查询服务器状态
     *
     * @param ip
     * @param date
     * @return
     */
    public List<ServerStatus> queryServerStat(String ip, Date date) {
        try {
            return serverStatusDao.queryServerStat(ip, date);
        } catch (Exception e) {
            logger.error("queryServerStat err ip:{}, date:{}", ip, date, e);
        }
        return Collections.emptyList();
    }

    /**
     * 查询服务器状态
     *
     * @param ip
     * @param date
     * @return
     */
    public List<ServerStatus> queryServerStatByIp(String ip, Date date, String beginTime) {
        try {
            return serverStatusDao.queryServerStatByIp(ip, date, beginTime);
        } catch (Exception e) {
            logger.error("queryServerStat err ip:{}, date:{}", ip, date, e);
        }
        return Collections.emptyList();
    }

    /**
     * 保存服务器状态
     *
     * @param server
     */
    public void saveServerStat(Server server, boolean force) {
        if (server == null || server.getDateTime() == null) {
            return;
        }
        try {
            if (force) {
                serverStatusDao.saveAndUpdateServerStat(server);
            } else {
                serverStatusDao.saveServerStat(server);
            }
        } catch (Exception e) {
            logger.error("saveServerStat err server:{}", server, e);
        }
    }

    /**
     * 删除数据
     *
     * @param date
     * @return
     */
    public Result<Integer> delete(Date date) {
        Integer rows = 0;
        try {
            rows = serverStatusDao.deleteServerStat(date);
        } catch (Exception e) {
            logger.error("dete err, date:{}", date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(rows);
    }

    /**
     * 删除机器
     *
     * @param ip
     * @return
     */
    @Transactional
    public Result<Integer> deleteServer(String ip) {
        try {
            // 第一步删除机器
            Integer count = serverStatusDao.deleteServer(ip);
            if (count == null || count != 1) {
                return Result.getResult(Status.DB_ERROR);
            }
            // 第二步 删除当前机器的报警配置
            Integer deleteCount = serverAlarmConfigDao.delete(ip);
            if (deleteCount == null) {
                return Result.getResult(Status.DB_ERROR);
            }
        } catch (Exception e) {
            logger.error("deleteServer err, ip:{}", ip, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 修改数据
     *
     * @param ip
     * @param type
     * @return
     */
    public Result<Integer> updateServer(String ip, int type) {
        Integer rows = 0;
        try {
            rows = serverStatusDao.updateServer(ip, type);
        } catch (Exception e) {
            logger.error("updateServer err, ip:{}", ip, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(rows);
    }

    /**
     * 收集系统状况
     *
     * @param ip
     */
    public void collectServerStatus(ServerInfo serverInfo) throws SSHException {
        String ip = serverInfo.getIp();
        final Server server = new Server();
        server.setIp(ip);
        SSHResult result = sshTemplate.execute(ip, new SSHCallback() {
            public SSHResult call(SSHSession session) {
                return session.executeCommand(COLLECT_SERVER_STATUS, new DefaultLineProcessor() {
                    public void process(String line, int lineNum) throws Exception {
                        server.parse(line, null);
                    }
                });
            }
        });
        server.resetDateTime();
        if (!result.isSuccess()) {
            logger.error("collect " + ip + " err:" + result.getResult(), result.getExcetion());
        }
        //保存服务器静态信息
        saveAndUpdateServerInfo(server);
        //保存服务器状况信息
        if (serverInfo.getCollectTime() != null) {
            server.setCollectTime(serverInfo.getCollectTime());
            saveServerStat(server, true);
        } else {
            saveServerStat(server, false);
        }
    }

    /**
     * 抓取所有服务器状态
     */
    public void fetchAllServerStatus() {
        List<ServerInfo> serverInfoList = queryAllServerInfo();
        Map<String, List<String>> deployInfo = queryDeployInfo();
        for (ServerInfo server : serverInfoList) {
            server.setDeployDirs(deployInfo.get(server.getIp()));
            fetchServerStatus(server);
        }
    }

    public Map<String, List<String>> queryDeployInfo() {
        Map<String, List<String>> deployInfo = new HashMap<>();
        add2Map(brokerService.queryAll(), deployInfo);
        add2Map(nameServerService.queryAll(), deployInfo);
        add2Map(proxyService.queryAll(), deployInfo);
        add2Map(controllerService.queryAll(), deployInfo);
        return deployInfo;
    }

    private void add2Map(Result<?> result, Map<String, List<String>> deployInfo) {
        if (result.isEmpty()) {
            return;
        }
        List<?> list = (List<?>) result.getResult();
        for (Object component : list) {
            DeployableComponent deployableComponent = (DeployableComponent) component;
            if (!StringUtils.isEmpty(deployableComponent.getBaseDir())) {
                deployInfo.computeIfAbsent(deployableComponent.getIp(), k -> new ArrayList<>()).add(deployableComponent.getBaseDir());
            }
        }
    }

    /**
     * 抓取服务器状态
     */
    public void fetchServerStatus(ServerInfo serverInfo) {
        try {
            //尝试收集服务器运行状况
            collectServerStatus(serverInfo);
            //启动nmon收集服务器运行状况
            OSInfo info = nmonService.start(serverInfo);
            if (info == null) {
                return;
            }
            saveServerInfo(serverInfo.getIp(), info.getIssue(), -1, null);
        } catch (Exception e) {
            logger.error("fetchServerStatus:{} err", serverInfo.getIp(), e);
        }
    }
}
