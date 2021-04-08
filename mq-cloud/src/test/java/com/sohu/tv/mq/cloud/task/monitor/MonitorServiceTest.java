package com.sohu.tv.mq.cloud.task.monitor;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MonitorServiceTest {

    @Autowired
    private NameServerService nameServerService;
    
    @Autowired
    private SohuMonitorListener sohuMonitorListener;
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @Autowired
    private ConsumerService consumerService;
    
    @Test
    public void test() throws MQClientException, RemotingException, InterruptedException {
        MonitorService monitorService = new MonitorService(nameServerService, clusterService.getMQClusterById(2), 
                sohuMonitorListener, mqCloudConfigHelper);
        monitorService.setConsumerService(consumerService);
        monitorService.doMonitorWork();
    }
}
