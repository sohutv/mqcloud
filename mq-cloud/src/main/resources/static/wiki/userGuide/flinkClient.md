# 前置配置

1. <span id="pom">pom依赖</span>

   ```
   <dependency>
		<groupId>com.sohu.tv</groupId>
		<artifactId>mq-flink-client-sohu</artifactId>
		<version>${version}</version>
	</dependency>
   <repository>
       <id>sohu.nexus</id>
       <url>${repositoryUrl}</url>
   </repository>
   ```

2. <span id="logback">日志配置</span>

   在类路径添加日志配置文件[rmq.logback.xml](rmq.logback.xml)，名称不可更改，文件内容参考如下：

   ```
   <?xml version="1.0" encoding="UTF-8"?>
   <configuration>
       <appender name="rmqAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
           <file>${LOGS_DIR}/rocketmq.log</file>
           <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
               <fileNamePattern>${LOGS_DIR}/otherdays/rocketmq.log.%d{yyyy-MM-dd}</fileNamePattern>
               <maxHistory>40</maxHistory>
           </rollingPolicy>
           <encoder>
               <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} {%thread} %-5level %logger{50}-%L - %msg%n</pattern>
               <charset class="java.nio.charset.Charset">UTF-8</charset>
           </encoder>
       </appender>
       <root level="INFO">
           <appender-ref ref="rmqAppender" />
       </root>
   </configuration>
   ```
## 一、<span id="source">source 接入</span>

```
// for local test
StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnviron

// build source config
FlinkSourceConfig flinkSourceConfig = new FlinkSourceConfig();
flinkSourceConfig.setConsumerGroup("basic-test-consumer");
flinkSourceConfig.setTopic("basic-test-topic");

// create SimpleStringDeserializationSchema
SimpleStringDeserializationSchema schema = new SimpleStringDeserializationSchema();

// add mq-flink source
DataStreamSource<String> source = env.addSource(new MQClientSourceFunction<>(schema, flinkSourceConfig))
        .setParallelism(4);

// handle message
source.map(new MapFunction<String, String>() {
    @Override
    public String map(String s) throws Exception {
        return s;
    }
});
// run
try {
    env.execute("sohu-flink-test");
} catch (Exception e) {
    e.printStackTrace();
}
```
客户端内置2种简单的消息解码器，如下：

-  SimpleStringDeserializationSchema：返回String类型的消息体
-  StringDeserializationSchema：返回二元组，f0为String类型的消息体，f1为消息对象MessageExt

如需要自定义实现消息解码，请实现MessageExtDeserializationSchema接口。

## 二、<span id="sink">sink 接入</span>
```
// build sink config
FlinkSinkConfig flinkSinkConfig = new FlinkSinkConfig();
flinkSinkConfig.setTopic("mqcloud-http-test-topic");
flinkSinkConfig.setProducerGroup("mqcloud-http-test-topic-producer");

// build message SerializationSchema
SimpleStringSerializationSchema serializationSchema = new SimpleStringSerializationSchema();

// sync send message
…………
.addSink(new MQClientSink(flinkSinkConfig, serializationSchema))
                .setParallelism(2);
```

**注意：** MQClientSink接受Tuple2<Object, String>类型输入，其中f0为消息体，f1为Key，可在输入前进行map转化，示例：
```
source.map(new MapFunction<String, Tuple2<Object, String>>() {
            @Override
            public Tuple2<Object, String> map(String messageBody) throws Exception {
                // return Tuple2
                return Tuple2.of(messageBody, key);
            }
        }).addSink(new MQClientSink(flinkSinkConfig, serializationSchema))
                .setParallelism(2);
```
客户端内置简单的消息序列化器，即SimpleStringSerializationSchema，采用FastJson进行序列化，如需自定义实现序列化，请实现接口MessageExtSerializationSchema

**发送方式：** sink支持同步，异步，批量发送，如下：

- 同步
  ```
  addSink(new MQClientSink(flinkSinkConfig, serializationSchema))
                .setParallelism(2);
   ```

- 异步
  ```
  .addSink(
                new MQClientSink(flinkSinkConfig, serializationSchema)
                        .withAsync(true)
                )
                .setParallelism(2);
   ```

- 批量
  ```
  .addSink(
                new MQClientSink(flinkSinkConfig, serializationSchema)
                        .withBatchSend(true)
                        .withBatchSize(315)
                        .withMaxWindowsTime(100))
                .setParallelism(2);
  ```
  **注意：** 批量发送的队列剩余消息依赖**checkPoint**机制检查刷新，需要增加以下配置：
    ```
    env.enableCheckpointing(10000);
    env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
  ```        

