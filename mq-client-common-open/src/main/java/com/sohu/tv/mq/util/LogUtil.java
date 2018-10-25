package com.sohu.tv.mq.util;

import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;

import com.sohu.index.tv.mq.common.Result;

/**
 * 方便客户端记录日志
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年2月12日
 */
public class LogUtil {

    /**
     * 记录日志
     * 
     * @param logger
     * @param result
     * @param info
     */
    public static void log(Logger logger, Result<SendResult> result, Object info) {
        if (info == null) {
            info = "";
        }
        if (result == null) {
            logger.warn("rocketmq send, info:{}, return is null!", info);
            return;
        }
        if (!result.isSuccess()) {
            if (result.getException() == null) {
                logger.warn("rocketmq send, info:{}, rst:{}", info, result.getResult());
            } else {
                logger.error("rocketmq send err, info:{}, rst:{}", info, result.getResult(), result.getException());
            }
        }
    }
}
