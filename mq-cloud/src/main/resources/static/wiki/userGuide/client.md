## 一、<span id="pom">pom依赖</span>

```
<dependency>
    <groupId>com.sohu.tv</groupId>
    <artifactId>${clientArtifactId}</artifactId>
    <version>${version}</version>
</dependency>
<repository>
    <id>sohu.nexus</id>
    <url>${repositoryUrl}</url>
</repository>
```

## 二、<span id="logback">日志配置</span>

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

无论项目中使用的是log4j还是lo4j2，都可用此方式配置RocketMQ的日志，因为RocketMQ内部已经集成了logback。