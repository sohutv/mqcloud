package com.sohu.tv.mq.cloud.task.server.nmon;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.service.SSHTemplate.DefaultLineProcessor;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHSession;
import com.sohu.tv.mq.cloud.task.server.data.OS;
import com.sohu.tv.mq.cloud.task.server.data.OSInfo;
import com.sohu.tv.mq.cloud.util.OSFactory;
/**
 * 服务器监控脚本服务(nmon识别和监控)
 * @Description: 
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
	//nmon监控启动
	public static final String START_SERVER_COLLECT = NMON_FILE+" -F " + NMON_LOG + " -s0 -c1;" +
			"/bin/grep TCP /proc/net/sockstat > " + SOCK_LOG + 
			";ulimit -n -u > " + ULIMIT_LOG;
	
	@Autowired
	private NMONFileFinder nmonFileFinder;
	
	/**
	 * 启动nmon收集系统状况
	 * @param ip
	 * @param session
	 * @return @OSInfo 收集到的操作系统信息
	 */
	public OSInfo start(String ip, SSHSession session) {
		SSHResult startCollectResult = session.executeCommand(START_SERVER_COLLECT);
		if(!startCollectResult.isSuccess()) {
			logger.error("start nmon "+ip+" err:"+startCollectResult.getResult(), 
					startCollectResult.getExcetion());
			//执行命令没有发生异常，则nmon可能不存在或有问题
			if(startCollectResult.getExcetion() == null) {
				//尝试处理出错信息
				return initNmon(ip, session);
			}
		}
		return null;
	}
	
	/**
	 * 尝试修复启动失败的错误
	 * @param ip
	 * @param session
	 */
	private OSInfo initNmon(String ip, SSHSession session) {
		//获取nmon版本
		String version = getNMONVersion(ip, session);
		//获取操作系统原始信息
		OSInfo osInfo = getOSInfo(ip, session);
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
		sendNMONToServer(ip, session, nmonFile);
		
		return osInfo;
	}
	
	/**
	 * 获取nmon文件版本
	 * @param ip
	 * @param session
	 * @return 存在返回版本，不存在返回null, 执行错误返回异常
	 */
	private String getNMONVersion(String ip, SSHSession session) {
		SSHResult nmonVersionResult = session.executeCommand(NMON_VERSION);
		if(nmonVersionResult.isSuccess()) {
			return nmonVersionResult.getResult();
		} else {
			logger.error(NMON_VERSION+" err:"+nmonVersionResult.getResult(), nmonVersionResult.getExcetion());
		}
		return null;
	}
	
	/**
	 * 获取操作系统信息
	 * @param ip
	 * @param session
	 * @return OSInfo
	 */
	private OSInfo getOSInfo(String ip, SSHSession session) {
		final OSInfo osInfo = new OSInfo();
		session.executeCommand(OS_INFO_CMD, new DefaultLineProcessor() {
			public void process(String line, int lineNum) throws Exception {
				switch(lineNum) {
				case 1:
					osInfo.setUname(line);
					break;
				case 2:
					osInfo.setIssue(line);
				}
			}
		});
		return osInfo;
	}
	
	/**
	 * 将nmon文件scp到服务器上
	 * @param ip
	 * @param session
	 * @param nmonFile
	 */
	private void sendNMONToServer(String ip, SSHSession session, File nmonFile) {
		SSHResult scpRst = session.scpToFile(nmonFile.getAbsolutePath(), NMON_DIR + NMON);
		if(scpRst.isSuccess()) {
			logger.info("scp {} to {} success", nmonFile.getAbsolutePath(), ip);
		} else {
			logger.error("scp to "+ip+" err", scpRst.getExcetion());
		}
	}
}
