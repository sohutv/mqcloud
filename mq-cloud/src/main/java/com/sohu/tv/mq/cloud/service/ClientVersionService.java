package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.ClientVersion;
import com.sohu.tv.mq.cloud.dao.ClientVersionDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 客户端版本
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月31日
 */
@Service
public class ClientVersionService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ClientVersionDao clientVersionDao;
    
    /**
     * 保存客户端版本
     * @param clientVersion
     * @return
     */
    public Result<Integer> save(ClientVersion clientVersion){
        Integer result = null;
        try {
            result = clientVersionDao.insert(clientVersion);
        } catch (Exception e) {
            logger.error("insert err, clientVersion:{}", clientVersion, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 查询所有客户端版本
     * @return
     */
    public Result<List<ClientVersion>> queryAll(){
        List<ClientVersion> list = null;
        try {
            list = clientVersionDao.selectAll();
        } catch (Exception e) {
            logger.error("queryAll", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
}
