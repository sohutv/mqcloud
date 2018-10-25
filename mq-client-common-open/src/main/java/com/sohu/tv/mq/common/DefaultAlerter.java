package com.sohu.tv.mq.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 警报器默认实现
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年1月24日
 */
public class DefaultAlerter implements Alerter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAlerter.class);

    private static final DefaultAlerter instance = new DefaultAlerter();

    private DefaultAlerter() {
    }

    public static DefaultAlerter getInstance() {
        return instance;
    }

    public void alert(String info) {
        logger.error(info);
    }

}
