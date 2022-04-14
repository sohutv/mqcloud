package com.sohu.tv.mq.rocketmq.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPullConsumerImpl;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 继承DefaultMQPullConsumerImpl仅为了屏蔽rebalance方法
 * @author yongfeigao
 * @date 2021年12月6日
 */
@SuppressWarnings("deprecation")
public class NORebalanceDefaultMQPullConsumer extends DefaultMQPullConsumerImpl {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public NORebalanceDefaultMQPullConsumer(DefaultMQPullConsumer defaultMQPullConsumer, RPCHook rpcHook) {
        super(defaultMQPullConsumer, rpcHook);
    }

    /**
     * 不必rebalance
     */
    @Override
    public void doRebalance() {
        logger.info("no need rebalance");
    }
}
