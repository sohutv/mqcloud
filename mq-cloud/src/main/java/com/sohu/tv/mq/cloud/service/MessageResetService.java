package com.sohu.tv.mq.cloud.service;
/**
 * 消息重置
 * 
 * @author yongfeigao
 * @date 2019年10月28日
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.MessageReset;
import com.sohu.tv.mq.cloud.dao.MessageResetDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 消息重置
 * 
 * @author yongfeigao
 * @date 2019年10月28日
 */
@Service
public class MessageResetService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MessageResetDao messageResetDao;
    
    /**
     * 保存messageReset记录
     * 
     * @param messageReset
     * @return 返回Result
     */
    public Result<?> save(MessageReset messageReset) {
        try {
            messageResetDao.insert(messageReset);
        } catch (Exception e) {
            logger.error("insert err, messageReset:{}", messageReset, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 查询MessageReset
     * 
     * @return Result<MessageReset>
     */
    public Result<MessageReset> query(String consumer) {
        MessageReset messageReset = null;
        try {
            messageReset = messageResetDao.select(consumer);
        } catch (Exception e) {
            logger.error("query err, consumer:{}", consumer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(messageReset);
    }
}
