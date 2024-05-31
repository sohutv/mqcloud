/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sohu.tv.mq.flink.sink;

import com.sohu.tv.mq.flink.common.config.FlinkSinkProducerConfig;
import com.sohu.tv.mq.flink.common.serialization.MessageExtSerializationSchema;
import com.sohu.tv.mq.flink.common.util.CheckUtils;
import com.sohu.tv.mq.flink.common.util.MetricUtils;
import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.rocketmq.RocketMQProducer;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Meter;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * MQClientSink is transformed by open source RocketMQ-Flink, depending on mq-Client
 */
public class MQClientSink extends RichSinkFunction<Tuple2<Object, String>> implements CheckpointedFunction {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(MQClientSink.class);

    private transient RocketMQProducer producer;
    
    private boolean async; // false by default

    private final FlinkSinkProducerConfig producerConfig;

    private boolean open;

    private int batchSize = 32;

    private int maxWindowsTime = 2000;

    private final long initial_backoff = 200;

    private long nextSendTimeStamp = System.currentTimeMillis();

    private int maxMessageSize = 1024 * 1024 * 4;

    public final int message_surplus_size = 250;
    
    private final MessageExtSerializationSchema messageExtSerializationSchema;

    private long totalSize;

    private List<Message> messageQueue;

    private Meter sinkInTps;
    private Meter outTps;
    private Meter outBps;
    private MetricUtils.LatencyGauge latencyGauge;

    public MQClientSink(FlinkSinkProducerConfig producerConfig, MessageExtSerializationSchema messageExtSerializationSchema) {
        this.producerConfig = producerConfig;
        this.messageExtSerializationSchema = messageExtSerializationSchema;
    }

    /**
     * 初始化producer客户端
     * 初始化指标
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        producer = new RocketMQProducer(producerConfig.getProducerGroup(), producerConfig.getTopic());
        producerConfig.attributeAssignment(producer);
        CheckUtils.checkProducerConfig(producerConfig);
        int indexOfThisSubtask = getRuntimeContext().getIndexOfThisSubtask();
        if (producerConfig.getInstanceName() == null) {
            producer.setInstanceName("T" + indexOfThisSubtask);
        } else {
            producer.setInstanceName(producerConfig.getInstanceName() + "#" + indexOfThisSubtask);
        }
        try {
            producer.start();
            LOG.debug("Flink producer start success.");
        } catch (Exception e) {
            LOG.error("Flink sink init failed, due to the producer cannot be initialized.");
            throw new RuntimeException(e);
        }
        // init batch send
        messageQueue = new LinkedList<>();

        sinkInTps = MetricUtils.registerSinkInTps(getRuntimeContext());
        outTps = MetricUtils.registerOutTps(getRuntimeContext());
        outBps = MetricUtils.registerOutBps(getRuntimeContext());
        latencyGauge = MetricUtils.registerOutLatency(getRuntimeContext());
    }

    /**
     * 消息封装序列化||消息收集
     * @param sendMessage The input record.
     * @param context Additional context about the input record.
     * @throws Exception
     */
    @Override
    public void invoke(Tuple2<Object, String> sendMessage, Context context) throws Exception {
        sinkInTps.markEvent();
        long timeStartWriting = System.currentTimeMillis();
        byte[] bodyToByte = messageExtSerializationSchema.serializeMessage(sendMessage.f0);
        Message message = new Message();
        message.setBody(bodyToByte);
        message.setTopic(producerConfig.getTopic());
        message.setKeys(sendMessage.f1);
        if (open) {
            sendWithBatch(message);
            return;
        }
        if (async) {
            sendWithAsync(message, timeStartWriting);
        } else {
            send(message, timeStartWriting);
        }
    }

    /**
     * 批量发送
     * @param input
     * @throws Exception
     */
    public void sendWithBatch(Message input) throws Exception {
        messageQueue.add(input);
        totalSize += input.getBody().length;
        if (isSend()) {
            flushBatch();
        }
    }

    /**
     * 同步发送
     * @param input
     * @param timeStartWriting
     * @throws RemotingException
     */
    public void send(Message input, Long timeStartWriting) throws RemotingException {
        int retries = 0;
        Result<SendResult> result = null;
        do {
            try {
                result = producer.publish(input);
                if (!result.isSuccess()) {
                    throw new RemotingException(result.toString());
                }
                long end = System.currentTimeMillis();
                latencyGauge.report(end - timeStartWriting, 1);
                outTps.markEvent();
                outBps.markEvent(input.getBody().length);
                break;
            } catch (Exception ex) {
                if (retries >= producerConfig.getRetryCount()) {
                    LOG.error("Sync send message exception: ", ex);
                    throw ex;
                }
                LOG.warn("{}, retry {}/{}", ex.getMessage(), retries, producerConfig.getRetryCount(), ex);
                retries++;
            }
            waitForMs(initial_backoff);
        } while (true);
        LOG.debug("Sync send message success! send result: {}", result);
    }

    /**
     * 异步发送
     * @param input
     * @param timeStartWriting
     */
    public void sendWithAsync(Message input, Long timeStartWriting) {
        try {
            SendCallback sendCallback =
                    new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            LOG.debug("Async send message success! result: {}", sendResult);
                            long end = System.currentTimeMillis();
                            latencyGauge.report(end - timeStartWriting, 1);
                            outTps.markEvent();
                            outBps.markEvent(input.getBody().length);
                        }

                        @Override
                        public void onException(Throwable throwable) {
                            if (throwable != null) {
                                LOG.error("Async send message failure!", throwable);
                            }
                        }
                    };
            producer.publishAsync(input, sendCallback);
        } catch (Exception e) {
            LOG.error("Async send message failure!", e);
        }
    }

    /**
     * 批量发送开关
     * @return
     */
    public boolean isSend() {
        if (messageQueue.size() >= batchSize) {
            return true;
        }
        if (System.currentTimeMillis() > nextSendTimeStamp && messageQueue.size() > 0) {
            return true;
        }
        return totalSize + (long) messageQueue.size() * message_surplus_size >= maxMessageSize;
    }

    public void flushBatch() throws Exception {
        synchronized (producer){
            if (messageQueue.size() > 0) {
                try {
                    Result<SendResult> result = producer.publish(messageQueue);
                    LOG.debug("batch send message success! the result: {}", result);
                    if (!result.isSuccess()) {
                        throw new RemotingException(result.toString());
                    }
                } catch (Exception e) {
                    LOG.error("Sync send message exception: ", e);
                    throw e;
                }
            }
            nextSendTimeStamp = System.currentTimeMillis() + maxWindowsTime;
            messageQueue.clear();
            totalSize = 0;
        }
    }

    /**
     * 批量消息全部刷入
     */
    public void flushAll() {
        if (messageQueue.size() > 0) {
            messageQueue.forEach(
                    message -> {
                        try {
                            Result<SendResult> result = producer.publish(message);
                            LOG.debug("batch send message result: {}", result);
                            if (!result.isSuccess()) {
                                throw new RemotingException(result.toString());
                            }
                        } catch (Exception e) {
                            LOG.error("batch send message exception: ", e);
                            throw new RuntimeException(e);
                        }
                    });
        }
        nextSendTimeStamp = System.currentTimeMillis() + maxWindowsTime;
        messageQueue.clear();
        totalSize = 0;
    }

    /**
     * 等待方法
     * @param sleepMs
     */
    public void waitForMs(long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        if (producer != null) {
            try {
                flushAll();
            } catch (Exception e) {
                LOG.error("FlushSync failure!", e);
            }
            // make sure producer can be shutdown, thus current producerGroup will be unregistered
            producer.shutdown();
        }
    }

    public MQClientSink withAsync(boolean async) {
        this.async = async;
        return this;
    }

    public MQClientSink withBatchSend(boolean batchSend) {
        this.open = batchSend;
        return this;
    }

    public MQClientSink withBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public MQClientSink withMaxWindowsTime(int maxWindowsTime) {
        this.maxWindowsTime = maxWindowsTime;
        return this;
    }

    public MQClientSink withMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    @Override
    public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
        if (!isSend()){
            return;
        }
        synchronized (producer){
            flushBatch();
            LOG.debug("snapshotState success.");
        }
    }

    @Override
    public void initializeState(FunctionInitializationContext functionInitializationContext) throws Exception {
        // no need to restore state
    }
}
