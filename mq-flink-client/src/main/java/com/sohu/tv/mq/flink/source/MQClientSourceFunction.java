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

package com.sohu.tv.mq.flink.source;

import com.sohu.tv.mq.flink.common.config.FlinkSourceConsumerConfig;
import com.sohu.tv.mq.flink.common.serialization.MessageExtDeserializationSchema;
import com.sohu.tv.mq.flink.common.util.CheckUtils;
import com.sohu.tv.mq.flink.common.watermark.WaterMarkForAll;
import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.tv.mq.flink.common.util.MetricUtils;
import com.sohu.tv.mq.rocketmq.RocketMQConsumer;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Meter;
import org.apache.flink.metrics.MeterView;
import org.apache.flink.metrics.SimpleCounter;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The RocketMQSource is based on RocketMQ pull consumer mode, and provides exactly once reliability
 * guarantees when checkpoints are enabled. Otherwise, the source doesn't provide any reliability
 * guarantees.
 */
public class MQClientSourceFunction<OUT> extends RichParallelSourceFunction<OUT>
        implements ResultTypeQueryable<OUT> {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(MQClientSourceFunction.class);

    private volatile RunningChecker runningChecker;

    private transient RocketMQConsumer consumer;

    private MessageExtDeserializationSchema<OUT> schema;

    private WaterMarkForAll waterMarkForAll;

    private ScheduledExecutorService timer;

    private final FlinkSourceConsumerConfig consumerConfig;

    private volatile ReentrantLock updateLock;

    private Meter tpsMetric;

    public MQClientSourceFunction(MessageExtDeserializationSchema<OUT> schema, FlinkSourceConsumerConfig consumerConfig) {
        this.schema = schema;
        this.consumerConfig = consumerConfig;
    }

    /**
     * 水位 || 消费者 初始化
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        log.info("source open....");

        if (waterMarkForAll == null) {
            waterMarkForAll = new WaterMarkForAll(5000);
        }
        if (timer == null) {
            timer = Executors.newSingleThreadScheduledExecutor();
        }
        if (updateLock == null) {
            updateLock = new ReentrantLock();
        }
        runningChecker = new RunningChecker();
        runningChecker.setState(RunningChecker.State.RUNNING);
        Counter outputCounter =
                getRuntimeContext()
                        .getMetricGroup()
                        .counter(MetricUtils.METRICS_TPS + "_counter", new SimpleCounter());
        tpsMetric =
                getRuntimeContext()
                        .getMetricGroup()
                        .meter(MetricUtils.METRICS_TPS, new MeterView(outputCounter, 60));

        consumer = new RocketMQConsumer(consumerConfig.getConsumerGroup(), consumerConfig.getTopic());
        consumerConfig.attributeAssignment(consumer);
        CheckUtils.checkConsumerConfig(consumerConfig);
        int indexOfThisSubtask = getRuntimeContext().getIndexOfThisSubtask();
        if (consumer.getInstanceName() == null) {
            consumer.setInstanceName("T"+ indexOfThisSubtask);
        } else {
            consumer.setInstanceName(consumerConfig.getInstanceName() + "#" + indexOfThisSubtask);
        }
    }

    /**
     * 消费回调
     */
    class SourceConsumerCallBack implements ConsumerCallback<Object, MessageExt> {

        private SourceContext context;

        public SourceConsumerCallBack(SourceContext sourceContext) {
            this.context = sourceContext;
        }

        @Override
        public void call(Object s, MessageExt k) throws Exception {
            OUT data = schema.deserializeMessageBody(k);
            try {
                updateLock.lock();
                context.collectWithTimestamp(data, k.getBornTimestamp());
                waterMarkForAll.extractTimestamp(
                        k.getBornTimestamp());
                tpsMetric.markEvent();
            } finally {
                updateLock.unlock();
            }
        }
    }

    /**
     * 消费者启动
     * @param context The context to emit elements to and for accessing locks.
     * @throws Exception
     */
    @Override
    public void run(SourceContext context) throws Exception {
        timer.scheduleAtFixedRate(
                () -> {
                    context.emitWatermark(waterMarkForAll.getCurrentWatermark());
                },
                5,
                5,
                TimeUnit.SECONDS);
        consumer.setConsumerCallback(new SourceConsumerCallBack(context));
        consumer.start();
        // brocking main thread until the running status changed
        awaitTermination();

        if (runningChecker.isFailed()) {
            throw new RuntimeException(
                    "RunningChecker is failed, Please check the log in consumer thread");
        }
    }

    /**
     * 阻塞方法
     * @throws InterruptedException
     */
    private void awaitTermination() throws InterruptedException {
        while (runningChecker.isRunning()) {
            Thread.sleep(50);
        }
    }

    /**
     * task 关闭时执行
     */
    @Override
    public void cancel() {
        log.warn("cancel ...");

        if (runningChecker != null) {
            runningChecker.setState(RunningChecker.State.FINISHED);
        }

        if (timer != null) {
            timer.shutdown();
        }

        if (consumer != null) {
            consumer.shutdown();
        }
    }

    @Override
    public void close() throws Exception {
        log.warn("close ...");
        try {
            cancel();
        } finally {
            super.close();
        }
    }

    @Override
    public TypeInformation<OUT> getProducedType() {
        return schema.getProducedType();
    }
}
