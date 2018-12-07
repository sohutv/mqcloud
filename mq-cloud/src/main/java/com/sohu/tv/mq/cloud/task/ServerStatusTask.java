package com.sohu.tv.mq.cloud.task;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.bo.ServerInfo;
import com.sohu.tv.mq.cloud.service.SSHTemplate;
import com.sohu.tv.mq.cloud.service.SSHTemplate.DefaultLineProcessor;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHCallback;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHSession;
import com.sohu.tv.mq.cloud.service.ServerDataService;
import com.sohu.tv.mq.cloud.task.server.data.OSInfo;
import com.sohu.tv.mq.cloud.task.server.data.Server;
import com.sohu.tv.mq.cloud.task.server.nmon.NMONService;
import com.sohu.tv.mq.cloud.util.Result;

import net.javacrumbs.shedlock.core.SchedulerLock;

/**
 * 服务器状态监控任务
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月18日
 */
public class ServerStatusTask {
	private static final Logger logger = LoggerFactory.getLogger(ServerStatusTask.class);

	//获取监控结果
	public static final String COLLECT_SERVER_STATUS = 
			  "[ -e \""+NMONService.SOCK_LOG+"\" ] && /bin/cat " + NMONService.SOCK_LOG + " >> " + NMONService.NMON_LOG
			+ ";[ -e \""+NMONService.ULIMIT_LOG+"\" ] && /bin/cat " + NMONService.ULIMIT_LOG + " >> " + NMONService.NMON_LOG
			+ ";[ -e \""+NMONService.NMON_LOG+"\" ] && /bin/mv " + NMONService.NMON_LOG + " " + NMONService.NMON_OLD_LOG
			+ ";[ $? -eq 0 ] && /bin/cat " + NMONService.NMON_OLD_LOG;
	
	//nmon服务
	@Autowired
	private NMONService nmonService;
	//ssh 模板类
	@Autowired
	private SSHTemplate sshTemplate;
	//持久化
	@Autowired
	private ServerDataService serverDataService;
	
	@Autowired
    private TaskExecutor taskExecutor;
	
	@Scheduled(cron = "3 */5 * * * *")
    @SchedulerLock(name = "fetchServerStatus", lockAtMostFor = 240000, lockAtLeastFor = 240000)
	public void fetchServerStatus() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                List<ServerInfo> serverInfoList = serverDataService.queryAllServerInfo();
                for (ServerInfo server : serverInfoList) {
                    fetchServerStatus(server.getIp());
                } 
            }
        });
	}
	
    /**
     * 删除服务器统计数据
     */
    @Scheduled(cron = "0 30 4 * * ?")
    @SchedulerLock(name = "deleteServerStatus", lockAtMostFor = 240000, lockAtLeastFor = 240000)
    public void deleteServerStatus() {
        // 30天以前
        long now = System.currentTimeMillis();
        Date thirtyDaysAgo = new Date(now - 30L * 24 * 60 * 60 * 1000);
        logger.info("deleteServerStatus date:{}", thirtyDaysAgo);
        Result<Integer> result = serverDataService.delete(thirtyDaysAgo);
        if (result.isOK()) {
            logger.info("deleteServerStatus success, rows:{} use:{}ms",
                    result.getResult(), (System.currentTimeMillis() - now));
        } else {
            if (result.getException() != null) {
                logger.error("deleteServerStatus err", result.getException());
            } else {
                logger.info("deleteServerStatus failed");
            }
        }
    }
	
	/**
	 * 抓取服务器状态
	 * @param ip
	 */
	public void fetchServerStatus(final String ip) {
		try {
			sshTemplate.execute(ip, new SSHCallback() {
				public SSHResult call(SSHSession session) {
					//尝试收集服务器运行状况
					collectServerStatus(ip, session);
					//启动nmon收集服务器运行状况
					OSInfo info = nmonService.start(ip, session);
					saveServerStatus(ip, info);
					return null;
				}
			});
		} catch (Exception e) {
			logger.error("fetchServerStatus "+ip+" err", e);
		}
	}
	
	/**
	 * 收集系统状况
	 * @param ip
	 * @param session
	 */
	private void collectServerStatus(String ip, SSHSession session) {
		final Server server = new Server();
		server.setIp(ip);
		SSHResult result = session.executeCommand(COLLECT_SERVER_STATUS, new DefaultLineProcessor() {
			public void process(String line, int lineNum) throws Exception {
				server.parse(line, null);
			}
		});
		if(!result.isSuccess()) {
			logger.error("collect " + ip + " err:" + result.getResult(), result.getExcetion());
		}
		//保存服务器静态信息
		serverDataService.saveAndUpdateServerInfo(server);
		//保存服务器状况信息
		serverDataService.saveServerStat(server);
	}
	
	/**
	 * 保存服务器dist信息
	 * @param ip
	 * @param OSInfo
	 */
	private void saveServerStatus(String ip, OSInfo osInfo) {
		if(osInfo == null) {
			return;
		}
		serverDataService.saveServerInfo(ip, osInfo.getIssue(), -1);
	}
}
