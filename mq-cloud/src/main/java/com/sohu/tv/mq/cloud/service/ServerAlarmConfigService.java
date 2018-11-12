package com.sohu.tv.mq.cloud.service;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sohu.tv.mq.cloud.bo.ServerAlarmConfig;
import com.sohu.tv.mq.cloud.dao.ServerAlarmConfigDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 服务器预警配置项
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月31日
 */
@Service
public class ServerAlarmConfigService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ServerAlarmConfigDao serverAlarmConfigDao;

    /**
     * 查询报警配置
     * 
     * @return
     */
    public Result<ServerAlarmConfig> query(String ip) {
        ServerAlarmConfig conifg = null;
        try {
            conifg = serverAlarmConfigDao.selectByIp(ip);
        } catch (Exception e) {
            logger.error("selectByIp, ip:{}", ip, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(conifg);
    }

    /**
     * 查询报警配置
     * 
     * @return
     */
    public Result<List<ServerAlarmConfig>> queryAll() {
        List<ServerAlarmConfig> conifgList = null;
        try {
            conifgList = serverAlarmConfigDao.selectAll();
        } catch (Exception e) {
            logger.error("queryAll", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(conifgList);
    }

    /**
     * 修改报警配置
     * 
     * @return
     */
    public Result<Integer> update(ServerAlarmConfig serverAlarmConfig, Collection<String> ipCollection) {
        Integer count = null;
        try {
            count = serverAlarmConfigDao.update(serverAlarmConfig, ipCollection);
        } catch (Exception e) {
            logger.error("update err, serverAlarmConfig:{}", serverAlarmConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }
}
