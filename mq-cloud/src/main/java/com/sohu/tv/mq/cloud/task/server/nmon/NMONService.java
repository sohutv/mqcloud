package com.sohu.tv.mq.cloud.task.server.nmon;

import com.google.common.base.Joiner;
import com.sohu.tv.mq.cloud.bo.ServerInfo;
import com.sohu.tv.mq.cloud.service.SSHTemplate;
import com.sohu.tv.mq.cloud.service.SSHTemplate.DefaultLineProcessor;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.task.server.data.OS;
import com.sohu.tv.mq.cloud.task.server.data.OSInfo;
import com.sohu.tv.mq.cloud.util.OSFactory;
import com.sohu.tv.mq.cloud.util.SSHException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
/**
 * 服务器监控脚本服务(nmon识别和监控)
 * @author yongfeigao
 * @date 2018年7月18日
 */
@Component
public class NMONService {
	private static final Logger logger = LoggerFactory.getLogger(NMONService.class);
	//获取系统版本位数命令
	public static final String OS_INFO_CMD = "/bin/uname -a; /bin/cat /etc/issue";
	//nmon路径
	public static final String NMON_DIR = "/tmp/";
	//nmon文件名
	public static final String NMON = "nmon";
	//nmon完整路径
	public static final String NMON_FILE = NMON_DIR + NMON;
	//获取nmon版本
	public static final String NMON_VERSION = "[ -e \""+NMON_FILE+"\" ] && "+NMON_FILE+" -V";
	//nmon输出的结果文件
	public static final String NMON_LOG = "/tmp/nmon_mq.log";
	//nmon输出的老结果文件
	public static final String NMON_OLD_LOG = "/tmp/nmon_mq.old.log";
	//tcp输出的结果文件
	public static final String SOCK_LOG = "/tmp/sock_mq.log";
	//ulimit输出的结果文件
	public static final String ULIMIT_LOG = "/tmp/ulimit_mq.log";
	//disk输出的结果文件
	public static final String DISK_LOG = "/tmp/disk_mq.log";
	public static final String DISK_MQ_FLAG = "MQDISK";
	//nmon监控启动
	public static final String START_SERVER_COLLECT = NMON_FILE+" -F " + NMON_LOG + " -s0 -c1;" +
			"/bin/grep TCP /proc/net/sockstat > " + SOCK_LOG + 
			";ulimit -n -u > " + ULIMIT_LOG;
	
	@Autowired
	private NMONFileFinder nmonFileFinder;

	@Autowired
	private SSHTemplate sshTemplate;
	
	/**
	 * 启动nmon收集系统状况
	 * @return @OSInfo 收集到的操作系统信息
	 */
	public OSInfo start(ServerInfo serverInfo) throws SSHException {
		String command = START_SERVER_COLLECT;
		if (!CollectionUtils.isEmpty(serverInfo.getDeployDirs())) {
			String deployDirs = Joiner.on(" ").join(serverInfo.getDeployDirs());
			command += ";/bin/df -m " + deployDirs + "|tail -n +2|sed 's/^/" + DISK_MQ_FLAG + " /' > " + DISK_LOG;
		}
		final String finalCommand = command;
		SSHResult result = sshTemplate.execute(serverInfo.getIp(), session -> session.executeCommand(finalCommand));
		if (!result.isSuccess()) {
			logger.error("{} start nmon:{} err", serverInfo.getIp(), result.getResult(), result.getExcetion());
			//执行命令没有发生异常，则nmon可能不存在或有问题
			if (result.getExcetion() == null) {
				//尝试处理出错信息
				return initNmon(serverInfo.getIp());
			}
		}
		return null;
	}
	
	/**
	 * 尝试修复启动失败的错误
	 * @param ip
	 */
	private OSInfo initNmon(String ip) throws SSHException {
		//获取nmon版本
		String version = getNMONVersion(ip);
		//获取操作系统原始信息
		OSInfo osInfo = getOSInfo(ip);
		OS os = null;
		//nmon文件不存在，需要根据操作系统识别是否支持
		if(null == version) {
			logger.warn("{} not exist {}", ip, NMON_FILE);
			//将原始信息转换为可识别的操作系统
			os = OSFactory.getOS(osInfo);
		} else {
			//nmon存在，但是版本有问题，此时不应该再判断系统信息了，直接用默认的  
			logger.warn("{} {} version err:"+version, ip, NMON_FILE);
			os = OSFactory.getDefaultOS(osInfo);
		}
		if(os == null) {
			logger.error("unkonw os info={}", osInfo);
			return null;
		}
		//获取nmon文件
		File nmonFile = nmonFileFinder.getNMONFile(os);
		if(nmonFile == null) {
			logger.warn("{} no corresponding nmon file", os);
			nmonFile = nmonFileFinder.getNMONFile(OSFactory.getDefaultOS(osInfo));
		}
		//将nmon文件传输至服务器
		sendNMONToServer(ip, nmonFile);
		
		return osInfo;
	}
	
	/**
	 * 获取nmon文件版本
	 * @param ip
	 * @return 存在返回版本，不存在返回null, 执行错误返回异常
	 */
	private String getNMONVersion(String ip) throws SSHException {
		SSHResult result = sshTemplate.execute(ip, session -> session.executeCommand(NMON_VERSION));
		if (result.isSuccess()) {
			return result.getResult();
		} else {
			logger.error("{} err:{}", NMON_VERSION, result.getResult(), result.getExcetion());
		}
		return null;
	}
	
	/**
	 * 获取操作系统信息
	 * @param ip
	 * @return OSInfo
	 */
	private OSInfo getOSInfo(String ip) throws SSHException {
		final OSInfo osInfo = new OSInfo();
		sshTemplate.execute(ip, session -> session.executeCommand(OS_INFO_CMD, new DefaultLineProcessor() {
			public void process(String line, int lineNum) {
				switch (lineNum) {
					case 1:
						osInfo.setUname(line);
						break;
					case 2:
						osInfo.setIssue(line);
				}
			}
		}));
		return osInfo;
	}
	
	/**
	 * 将nmon文件scp到服务器上
	 * @param ip
	 * @param nmonFile
	 */
	private void sendNMONToServer(String ip, File nmonFile) throws SSHException {
		SSHResult scpRst = sshTemplate.execute(ip, session -> session.scpToFile(nmonFile.getAbsolutePath(), NMON_DIR + NMON));
		if (scpRst.isSuccess()) {
			logger.info("scp {} to {} success", nmonFile.getAbsolutePath(), ip);
		} else {
			logger.error("scp to {} err", ip, scpRst.getExcetion());
		}
	}
}
