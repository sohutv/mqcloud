package com.sohu.tv.mq.cloud.service;
/**
 * 通用配置服务
 * 
 * @author yongfeigao
 * @date 2018年10月17日
 */

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.CommonConfig;
import com.sohu.tv.mq.cloud.dao.CommonConfigDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 通用配置服务
 * 
 * @author yongfeigao
 * @date 2018年10月17日
 */
@Service
public class CommonConfigService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private CommonConfigDao commonConfigDao;
    
    /**
     * 查询配置
     * 
     */
    public Result<List<CommonConfig>> query() {
        List<CommonConfig> list = null;
        try {
            list = commonConfigDao.select();
        } catch (Exception e) {
            logger.error("query err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    /**
     * 保存配置
     */
    public Result<Integer> save(CommonConfig commonConfig) {
        Integer result = null;
        try {
            result = commonConfigDao.insert(commonConfig);
        } catch (Exception e) {
            logger.error("save err{}", commonConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
}
