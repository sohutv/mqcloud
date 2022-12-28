package com.sohu.tv.mq.trace;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.trace.*;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.logging.InternalLogger;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * copy from AsyncTraceDispatcher 为了使用MQCloud独立的集群以及区域亲和
 *
 * @author: yongfeigao
 * @date: 2022/11/22 16:09
 */
public class SohuAsyncTraceDispatcher extends AsyncTraceDispatcher {
    private final static InternalLogger log = ClientLogger.getLog();
    private final static AtomicInteger COUNTER = new AtomicInteger();
    private final int queueSize;
    private final int batchSize;
    private final int maxMsgSize;
    private final DefaultMQProducer traceProducer;
    private final ThreadPoolExecutor traceExecutor;
    // The last discard number of log
    private AtomicLong discardCount;
    private Thread worker;
    private final ArrayBlockingQueue<TraceContext> traceContextQueue;
    private ArrayBlockingQueue<Runnable> appenderQueue;
    private volatile Thread shutDownHook;
    private volatile boolean stopped = false;
    private String dispatcherId = UUID.randomUUID().toString();
    private String traceTopic;

    public SohuAsyncTraceDispatcher(String traceTopic, DefaultMQProducer traceProducer) {
        super(null, null, null, null);
        // queueSize is greater than or equal to the n power of 2 of value
        this.queueSize = 2048;
        this.batchSize = 100;
        this.maxMsgSize = 128000 - 10 * 1000;
        this.traceTopic = traceTopic;
        this.traceProducer = traceProducer;
        this.discardCount = new AtomicLong(0L);
        this.traceContextQueue = new ArrayBlockingQueue<TraceContext>(1024);
        this.appenderQueue = new ArrayBlockingQueue<Runnable>(queueSize);
        this.traceExecutor = new ThreadPoolExecutor(//
                10, //
                20, //
                1000 * 60, //
                TimeUnit.MILLISECONDS, //
                this.appenderQueue, //
                new ThreadFactoryImpl("MQTraceSendThread_"));
    }

    public DefaultMQProducer getTraceProducer() {
        return traceProducer;
    }

    public void start(String nameSrvAddr, AccessChannel accessChannel) throws MQClientException {
        this.worker = new Thread(new SohuAsyncTraceDispatcher.AsyncRunnable(),
                "MQ-AsyncTraceDispatcher-Thread-" + dispatcherId);
        this.worker.setDaemon(true);
        this.worker.start();
        this.registerShutDownHook();
    }

    @Override
    public boolean append(final Object ctx) {
        boolean result = traceContextQueue.offer((TraceContext) ctx);
        if (!result) {
            log.info("buffer full" + discardCount.incrementAndGet() + " ,context is " + ctx);
        }
        return result;
    }

    @Override
    public void flush() {
        // The maximum waiting time for refresh,avoid being written all the time, resulting in failure to return.
        long end = System.currentTimeMillis() + 500;
        while (System.currentTimeMillis() <= end) {
            synchronized (traceContextQueue) {
                if (traceContextQueue.size() == 0 && appenderQueue.size() == 0) {
                    break;
                }
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
        log.info("------end trace send " + traceContextQueue.size() + "   " + appenderQueue.size());
    }

    @Override
    public void shutdown() {
        this.stopped = true;
        flush();
        this.traceExecutor.shutdown();
        traceProducer.shutdown();
        this.removeShutdownHook();
    }

    public void registerShutDownHook() {
        if (shutDownHook == null) {
            shutDownHook = new Thread(new Runnable() {
                private volatile boolean hasShutdown = false;

                @Override
                public void run() {
                    synchronized (this) {
                        if (!this.hasShutdown) {
                            flush();
                        }
                    }
                }
            }, "ShutdownHookMQTrace");
            Runtime.getRuntime().addShutdownHook(shutDownHook);
        }
    }

    public void removeShutdownHook() {
        if (shutDownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutDownHook);
            } catch (IllegalStateException e) {
                // ignore - VM is already shutting down
            }
        }
    }

    class AsyncRunnable implements Runnable {
        private boolean stopped;

        @Override
        public void run() {
            while (!stopped) {
                List<TraceContext> contexts = new ArrayList<TraceContext>(batchSize);
                synchronized (traceContextQueue) {
                    for (int i = 0; i < batchSize; i++) {
                        TraceContext context = null;
                        try {
                            //get trace data element from blocking Queue - traceContextQueue
                            context = traceContextQueue.poll(5, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                        }
                        if (context != null) {
                            contexts.add(context);
                        } else {
                            break;
                        }
                    }
                    if (contexts.size() > 0) {
                        SohuAsyncTraceDispatcher.AsyncAppenderRequest request =
                                new SohuAsyncTraceDispatcher.AsyncAppenderRequest(contexts);
                        traceExecutor.submit(request);
                    } else if (SohuAsyncTraceDispatcher.this.stopped) {
                        this.stopped = true;
                    }
                }
            }

        }
    }

    class AsyncAppenderRequest implements Runnable {
        List<TraceContext> contextList;

        public AsyncAppenderRequest(final List<TraceContext> contextList) {
            if (contextList != null) {
                this.contextList = contextList;
            } else {
                this.contextList = new ArrayList<TraceContext>(1);
            }
        }

        @Override
        public void run() {
            sendTraceData(contextList);
        }

        public void sendTraceData(List<TraceContext> contextList) {
            Map<String, List<TraceTransferBean>> transBeanMap = new HashMap<String, List<TraceTransferBean>>();
            for (TraceContext context : contextList) {
                if (context.getTraceBeans().isEmpty()) {
                    continue;
                }
                // Topic value corresponding to original message entity content
                String topic = context.getTraceBeans().get(0).getTopic();
                String regionId = context.getRegionId();
                // Use  original message entity's topic as key
                String key = topic;
                if (!StringUtils.isBlank(regionId)) {
                    key = key + TraceConstants.CONTENT_SPLITOR + regionId;
                }
                List<TraceTransferBean> transBeanList = transBeanMap.get(key);
                if (transBeanList == null) {
                    transBeanList = new ArrayList<TraceTransferBean>();
                    transBeanMap.put(key, transBeanList);
                }
                TraceTransferBean traceData = TraceDataEncoder.encoderFromContextBean(context);
                transBeanList.add(traceData);
            }
            for (Map.Entry<String, List<TraceTransferBean>> entry : transBeanMap.entrySet()) {
                String[] key = entry.getKey().split(String.valueOf(TraceConstants.CONTENT_SPLITOR));
                String dataTopic = entry.getKey();
                String regionId = null;
                if (key.length > 1) {
                    dataTopic = key[0];
                    regionId = key[1];
                }
                flushData(entry.getValue(), dataTopic, regionId);
            }
        }

        /**
         * Batch sending data actually
         */
        private void flushData(List<TraceTransferBean> transBeanList, String dataTopic, String regionId) {
            if (transBeanList.size() == 0) {
                return;
            }
            // Temporary buffer
            StringBuilder buffer = new StringBuilder(1024);
            int count = 0;
            Set<String> keySet = new HashSet<String>();

            for (TraceTransferBean bean : transBeanList) {
                // Keyset of message trace includes msgId of or original message
                keySet.addAll(bean.getTransKey());
                buffer.append(bean.getTransData());
                count++;
                // Ensure that the size of the package should not exceed the upper limit.
                if (buffer.length() >= maxMsgSize) {
                    sendTraceDataByMQ(keySet, buffer.toString(), dataTopic, regionId);
                    // Clear temporary buffer after finishing
                    buffer.delete(0, buffer.length());
                    keySet.clear();
                    count = 0;
                }
            }
            if (count > 0) {
                sendTraceDataByMQ(keySet, buffer.toString(), dataTopic, regionId);
            }
            transBeanList.clear();
        }

        /**
         * Send message trace data
         *
         * @param keySet the keyset in this batch(including msgId in original message not offsetMsgId)
         * @param data   the message trace data in this batch
         */
        private void sendTraceDataByMQ(Set<String> keySet, final String data, String dataTopic, String regionId) {
            final Message message = new Message(traceTopic, data.getBytes());
            // Keyset of message trace includes msgId of or original message
            message.setKeys(keySet);
            try {
                SendCallback callback = new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {

                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("send trace data failed, the traceData is {}", data, e);
                    }
                };
                traceProducer.send(message, callback, 5000);
            } catch (Exception e) {
                log.error("send trace data failed, the traceData is {}", data, e);
            }
        }
    }
}
