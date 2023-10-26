package com.sohu.tv.mq.cloud.task;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.common.Destroyable;

/**
 * 任务持有
 * 
 * @author yongfeigao
 * @date 2018年9月29日
 */
@Component("mqTaskExecutor")
public class TaskExecutor implements Destroyable {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private AtomicInteger counter = new AtomicInteger();

    /**
     * 执行任务
     * @param runable
     */
    public void execute(Runnable runable) {
        hold();
        try {
            runable.run();
        } finally {
            release();
        }
    }
    
    private void hold() {
        counter.incrementAndGet();
    }
    
    private void release() {
        counter.decrementAndGet();
    }

    @Override
    public void destroy() throws Exception {
        while(counter.get() > 0) {
            logger.info("{} task executing", counter.get());
            Thread.sleep(1000);
        }
    }

    @Override
    public int compareTo(Destroyable o) {
        return this.order() - o.order();
    }

    @Override
    public int order() {
        return 99;
    }

    @Override
    public String toString() {
        return "TaskExecutor [counter=" + counter.get() + "]";
    }
}
