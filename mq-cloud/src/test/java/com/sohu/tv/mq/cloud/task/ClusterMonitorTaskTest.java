package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ClusterMonitorTaskTest {

    @Autowired
    private ClusterMonitorTask monitorServiceTask;

    @Test
    public void testNameServerMonitor() {
        for (int i = 0; i < 1; i++) {
            monitorServiceTask.nameServerMonitor();
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testBrokerMonitorStatus() {
        for (int i = 0; i < 1; i++) {
            monitorServiceTask.brokerMonitor();
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testControllerMonitorStatus() {
        monitorServiceTask.controllerMonitor();
    }

    @Test
    public void testRetry() {
        monitorServiceTask.retry(()->{
            System.out.println("retry");
            return Result.getResult(Status.DB_ERROR);
        });
    }
}
