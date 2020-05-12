package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.sohu.tv.mq.cloud.bo.ServerInfo;
import com.sohu.tv.mq.cloud.bo.ServerInfoExt;
import com.sohu.tv.mq.cloud.bo.ServerStatus;
import com.sohu.tv.mq.cloud.dao.ServerAlarmConfigDao;
import com.sohu.tv.mq.cloud.dao.ServerStatusDao;
import com.sohu.tv.mq.cloud.task.server.data.Server;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
/**
 * 服务器数据
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月18日
 */
@Service
public class ServerDataService {
	private static final Logger logger = LoggerFactory.getLogger(ServerDataService.class);

    @Autowired
    private ServerStatusDao serverStatusDao;

    @Autowired
    private ServerAlarmConfigDao serverAlarmConfigDao;
	
	/**
	 * 查询server信息
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
     * @return List<ServerInfo>
     */
    public List<ServerInfo> queryAllServerInfo() {
        try {
            return serverStatusDao.queryAllServerInfo();
        } catch (Exception e) {
            logger.error("queryAllServerInfo err", e);
        }
        return new ArrayList<ServerInfo>(0);
    }
    
    /**
     * 查询今日当前所有服务器状态
     * @return List<ServerInfoExt>
     */
    public List<ServerInfoExt> queryAllServer(String date) {
        try {
            return serverStatusDao.queryAllServer(date);
        } catch (Exception e) {
            logger.error("queryAllServerStat err, date:{}", date, e);
        }
        return new ArrayList<ServerInfoExt>(0);
    }

	/**
	 * 保存服务器发行版信息
	 * @param ip
	 * @param dist
	 * @param type
	 */
	public Result<?> saveServerInfo(String ip, String dist, int type, String room) {
		if(dist == null) {
			return null;
		}
		dist = dist.trim();
		if(dist.length() == 0) {
			return null;
		}
		try {
			serverStatusDao.saveServerInfo(ip, dist, type, room);
		} catch (Exception e) {
			logger.error("saveServerInfo err:ip:{}, dist:{}, type:{}",ip, dist, type, e);
			return Result.getDBErrorResult(e);
		}
		return Result.getOKResult();
	}

	/**
	 * 保存/更新服务器信息
	 * @param server
	 * @return
	 */
	public Integer saveAndUpdateServerInfo(Server server) {
		if(server.getHost() == null || server.getNmon() == null || server.getCpus() == 0 || 
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
	 * @param ip
	 * @param date
	 * @return
	 */
	public List<ServerStatus> queryServerStat(String ip, String date) {
		try {
			return serverStatusDao.queryServerStat(ip, date);
		} catch (Exception e) {
			logger.error("queryServerStat err ip:{}, date:{}", ip, date, e);
		}
		return new ArrayList<ServerStatus>(0);
	}
	
	/**
     * 查询服务器状态
     * @param ip
     * @param date
     * @return
     */
    public List<ServerStatus> queryServerStatByIp(String ip, String date, String beginTime) {
        try {
            return serverStatusDao.queryServerStatByIp(ip, date, beginTime);
        } catch (Exception e) {
            logger.error("queryServerStat err ip:{}, date:{}", ip, date, e);
        }
        return new ArrayList<ServerStatus>(0);
    }
	
	/**
	 * 保存服务器状态
	 * @param server
	 */
	public void saveServerStat(Server server) {
		if(server == null || server.getDateTime() == null) {
			return;
		}
		try {
			serverStatusDao.saveServerStat(server);
		} catch (Exception e) {
			logger.error("saveServerStat err server:{}", server, e);
		}
	}
	
    /**
     * 删除数据
     * @param date
     * @return
     */
    public Result<Integer> delete(Date date) {
        Integer rows = 0;
        try {
            rows = serverStatusDao.deleteServerStat(DateUtil.formatYMD(date));
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
}
