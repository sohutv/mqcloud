package com.sohu.tv.mq.cloud.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 基于内存的MQ
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年3月5日
 * @param <T> 存储类型
 */
public class MemoryMQ<T> implements Destroyable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // 缓冲队列
    private BlockingQueue<T> bufferQueue;
    // 缓冲队列大小
    private int bufferSize = 1000;
    // 没有新数据时，等待多久处理一次，ms
    private int maxWaitWhenNoNewDataInMillis = 1000;
    // 从数量维度：一批最少处理的对象数
    private int minBatchDealSize = 10;
    // 从时间维度：等待多久处理一次，ms
    private int minDealIntervalMillis = 3000;
    // 从时间维度：minDealIntervalMillis内，最少有minDealIntervalBufferSize条数据才处理
    private int minDealIntervalBufferSize = 3;
    // 消费者名字
    private String consumerName = "memory-mq";
    // 消费者数量
    private int consumerThreadNum = 20;
    // 消费者线程池
    private ExecutorService consumerPool;
    // 是否已经停止
    private volatile boolean shutdown;
    // shutdown触发后，队列中还有数据的话，需要多少millis监测一次
    private int checkIntervaMillisWhenShutdownInvoked = 1000;
    // shutdown触发后，队列中还有数据的话，最多等待多少次监测
    private int maxCheckIntervaWhenShutdownInvoked = 20;
    // 消费者
    private MemoryMQConsumer<T> memoryMQConsumer;
    
    // 异常消息是否重新消费
    private boolean reconsume;
    // 销毁顺序
    private int destroyOrder;
    
    /**
     * 初始化
     */
    public void init() {
        bufferQueue = new ArrayBlockingQueue<T>(bufferSize);
        consumerPool = Executors.newFixedThreadPool(consumerThreadNum,
                new ThreadFactoryBuilder().setNameFormat(consumerName + "-%d").setDaemon(true).build());
        for (int i = 0; i < consumerThreadNum; ++i) {
            consumerPool.execute(new Runnable() {
                public void run() {
                    try {
                        consume();
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
        }
        logger.info("initialize with:{}", toString());
    }
    
    /**
     * 生产对象，成功返回true，否则false
     * @param t
     */
    public boolean produce(T t) {
        if(shutdown) {
            return false;
        }
        try {
            return bufferQueue.offer(t);
        } catch (Exception e) {
            logger.error("put err:{}", t, e);
        }
        return false;
    }

    /**
     * 消费
     */
    private void consume() {
        List<T> bufferList = new ArrayList<T>();
        long lastTimeMillis = System.currentTimeMillis();
        while (!shutdown) {
            T t = null;
            try {
                t = bufferQueue.poll(maxWaitWhenNoNewDataInMillis, TimeUnit.MICROSECONDS);
            } catch (InterruptedException e) {
                logger.error("queue.take err!", e);
            }
            if (t != null) {
                bufferList.add(t);
            }
            boolean timeup = System.currentTimeMillis() - lastTimeMillis > minDealIntervalMillis;
            if (bufferList.size() >= minBatchDealSize || (timeup && bufferList.size() >= minDealIntervalBufferSize)) {
                consume(bufferList);
                lastTimeMillis = System.currentTimeMillis() + (int) (60000 * Math.random());
            }
        }
        logger.info("shutdown invoked");
        // 处理剩余的数据
        consume(bufferList);
    }

    /**
     * 消费逻辑
     * 
     * @param list
     */
    private void consume(List<T> list) {
        long start = System.currentTimeMillis();
        if (list == null || list.size() == 0) {
            logger.error("list is empty");
            return;
        }
        for (T t : list) {
            try {
                memoryMQConsumer.consume(t);
            } catch (Exception e) {
                logger.error("consume {} err, reconsume:{}", t, reconsume, e);
                if(reconsume) {
                    boolean rst = produce(t);
                    if(!rst) {
                        logger.warn("reproduce {} err!", t);
                    }
                }
            }
        }
        int size = list.size();
        list.clear();
        long use = System.currentTimeMillis() - start;
        if (use >= 50) {
            logger.info("batch consume size:{} use:{}ms", size, use);
        }
    }

    /**
     * 关闭资源
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        if(shutdown) {
            logger.info("has shutdown over!!");
            return;
        }
        logger.info("shutdown invoked, waitting for producer...");
        int times = 0;
        while(bufferQueue.size() != 0) {
            Thread.sleep(checkIntervaMillisWhenShutdownInvoked);
            if(times++ >= maxCheckIntervaWhenShutdownInvoked) {
                break;
            }
        }
        logger.info("producer shutdown!!");
        shutdown = true;
        times = 0;
        consumerPool.shutdown();
        while(!consumerPool.isTerminated()) {
            logger.info("consumer left:{}", ((ThreadPoolExecutor)consumerPool).getActiveCount());
            Thread.sleep(checkIntervaMillisWhenShutdownInvoked);
            if(times++ >= maxCheckIntervaWhenShutdownInvoked) {
                break;
            }
        }
        logger.info("consumer shutdownOver!!");
    }
    
    public void setBufferQueue(BlockingQueue<T> bufferQueue) {
        this.bufferQueue = bufferQueue;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getMaxWaitWhenNoNewDataInMillis() {
        return maxWaitWhenNoNewDataInMillis;
    }

    public void setMaxWaitWhenNoNewDataInMillis(int maxWaitWhenNoNewDataInMillis) {
        this.maxWaitWhenNoNewDataInMillis = maxWaitWhenNoNewDataInMillis;
    }

    public int getMinBatchDealSize() {
        return minBatchDealSize;
    }

    public void setMinBatchDealSize(int minBatchDealSize) {
        this.minBatchDealSize = minBatchDealSize;
    }

    public int getMinDealIntervalMillis() {
        return minDealIntervalMillis;
    }

    public void setMinDealIntervalMillis(int minDealIntervalMillis) {
        this.minDealIntervalMillis = minDealIntervalMillis;
    }

    public int getMinDealIntervalBufferSize() {
        return minDealIntervalBufferSize;
    }

    public void setMinDealIntervalBufferSize(int minDealIntervalBufferSize) {
        this.minDealIntervalBufferSize = minDealIntervalBufferSize;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public int getConsumerThreadNum() {
        return consumerThreadNum;
    }

    public void setConsumerThreadNum(int consumerThreadNum) {
        this.consumerThreadNum = consumerThreadNum;
    }

    public ExecutorService getConsumerPool() {
        return consumerPool;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public MemoryMQConsumer<T> getMemoryMQConsumer() {
        return memoryMQConsumer;
    }

    public void setMemoryMQConsumer(MemoryMQConsumer<T> memoryMQConsumer) {
        this.memoryMQConsumer = memoryMQConsumer;
    }

    public void setReconsume(boolean reconsume) {
        this.reconsume = reconsume;
    }

    public boolean isReconsume() {
        return reconsume;
    }

    public void setDestroyOrder(int destroyOrder) {
        this.destroyOrder = destroyOrder;
    }

    @Override
    public int compareTo(Destroyable o) {
        return order() - o.order();
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }

    @Override
    public int order() {
        return destroyOrder;
    }

    @Override
    public String toString() {
        return "MemoryMQ [bufferQueue=" + bufferQueue + ", bufferSize=" + bufferSize + ", maxWaitWhenNoNewDataInMillis="
                + maxWaitWhenNoNewDataInMillis + ", minBatchDealSize=" + minBatchDealSize + ", minDealIntervalMillis="
                + minDealIntervalMillis + ", minDealIntervalBufferSize=" + minDealIntervalBufferSize + ", consumerName="
                + consumerName + ", consumerThreadNum=" + consumerThreadNum + ", consumerPool=" + consumerPool
                + ", shutdown=" + shutdown + ", checkIntervaMillisWhenShutdownInvoked="
                + checkIntervaMillisWhenShutdownInvoked + ", maxCheckIntervaWhenShutdownInvoked="
                + maxCheckIntervaWhenShutdownInvoked + ", memoryMQConsumer=" + memoryMQConsumer + ", reconsume="
                + reconsume + ", destroyOrder=" + destroyOrder + "]";
    }
}
