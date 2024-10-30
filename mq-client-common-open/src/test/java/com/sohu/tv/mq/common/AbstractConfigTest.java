package com.sohu.tv.mq.common;

import org.apache.rocketmq.common.ServiceState;
import org.apache.rocketmq.remoting.RPCHook;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @Auther: yongfeigao
 * @Date: 2023/12/12
 */
public class AbstractConfigTest {

    @Test
    public void testInitOK() {
        String topic = "mqcloud-test-topic";
        String group = "mqcloud-test-topic-producer";
        AbstractConfig config = new AbstractConfig(group, topic) {
            @Override
            protected int role() {
                return PRODUCER;
            }

            @Override
            public void setAclRPCHook(RPCHook rpcHook) {
            }

            @Override
            protected Object getMQClient() {
                return null;
            }

            @Override
            public ServiceState getServiceState() {
                throw new UnsupportedOperationException("not implement");
            }
        };
        config.setMqCloudDomain("127.0.0.1:8080");
        config.init();
    }

    @Test
    public void testInitFailed() {
        String topic = "mqcloud-test-topic";
        String group = "mqcloud-test-topic-produce";
        AbstractConfig config = new AbstractConfig(group, topic) {
            @Override
            protected int role() {
                return PRODUCER;
            }

            @Override
            public void setAclRPCHook(RPCHook rpcHook) {
            }

            @Override
            protected Object getMQClient() {
                return null;
            }

            @Override
            public ServiceState getServiceState() {
                throw new UnsupportedOperationException("not implement");
            }
        };
        config.setMqCloudDomain("127.0.0.1:8080");
        config.init();
    }
}