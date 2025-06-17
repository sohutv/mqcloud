package com.sohu.tv.mq.cloud.task;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务持有
 *
 * @author yongfeigao
 * @date 2018年9月29日
 */
@Component("mqTaskExecutor")
public class TaskExecutor implements SmartLifecycle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AtomicInteger counter = new AtomicInteger();

    @Autowired
    private LockProvider lockProvider;

    // 是否关闭
    private volatile boolean shutdown;

    private static final SimpleLock EMPTY_LOCK = () -> {};

    /**
     * 执行任务
     *
     * @param runnable
     */
    public void execute(Runnable runnable) {
        execute(runnable, null);
    }

    /**
     * 执行任务
     *
     * @param runnable
     */
    public void execute(Runnable runnable, LockConfiguration lockConfiguration) {
        if (shutdown) {
            return;
        }
        Optional<SimpleLock> lockOptional = lock(lockConfiguration);
        if (!lockOptional.isPresent()) {
            return;
        }
        hold();
        try {
            runnable.run();
        } finally {
            release();
            lockOptional.get().unlock();
        }
    }

    private Optional<SimpleLock> lock(LockConfiguration lockConfiguration) {
        // 没有lockConfiguration表示已经持有锁
        if (lockConfiguration == null) {
            return Optional.of(EMPTY_LOCK);
        }
        return lockProvider.lock(lockConfiguration);
    }

    private void hold() {
        counter.incrementAndGet();
    }

    private void release() {
        counter.decrementAndGet();
    }

    @Override
    public String toString() {
        return "TaskExecutor [counter=" + counter.get() + "]";
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        try {
            shutdown = true;
            while (counter.get() > 0) {
                logger.info("{} task executing", counter.get());
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isRunning() {
        return !shutdown;
    }

    @Override
    public int getPhase() {
        return 100;
    }
}
