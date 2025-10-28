package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConsumerTrafficServiceTest {
    
    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ConsumerTrafficService consumerTrafficService;
    
    @Test
    public void testCollectTraffic() {
        consumerTrafficService.collectTraffic(clusterService.getMQClusterById(8));
    }

}
