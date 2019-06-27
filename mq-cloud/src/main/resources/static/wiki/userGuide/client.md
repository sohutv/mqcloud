## 一、<span id="pom">pom依赖</span>

```
<dependency>
    <groupId>com.sohu.tv</groupId>
    <artifactId>${clientArtifactId}</artifactId>
    <version>${version}</version>
</dependency>
<repository>
    <id>sohu.nexus</id>
    <url>http://${nexusDomain}/nexus/content/groups/public</url>
</repository>
```

## 二、<span id="logback">日志配置-logback[可选]</span>

```
<appender name="RocketmqClientAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOGS_DIR}/rocketmq.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>${LOGS_DIR}/otherdays/rocketmq.log.%d{yyyy-MM-dd}
        </fileNamePattern>
        <maxHistory>40</maxHistory>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyy-MM-dd HH:mm:ss,GMT+8} %p %t - %m%n</pattern>
        <charset class="java.nio.charset.Charset">UTF-8</charset>
    </encoder>
</appender>
<logger name="RocketmqCommon" level="INFO" additivity="false">
    <appender-ref ref="RocketmqClientAppender"/>
</logger>
<logger name="RocketmqRemoting" level="INFO" additivity="false">
    <appender-ref ref="RocketmqClientAppender"/>
</logger>
<logger name="RocketmqClient" level="INFO" additivity="false">
    <appender-ref ref="RocketmqClientAppender"/>
</logger>
```

## 三、<span id="log4j">日志配置-log4j[可选]</span>

```
<appender name="RocketmqClientAppender" class="org.apache.log4j.DailyRollingFileAppender">
    <param name="File" value="${LOGS_DIR}/rocketmq.log" />
    <param name="DatePattern" value="'.'yyyy-MM-dd" />
    <layout class="org.apache.log4j.PatternLayout">
        <param name="ConversionPattern" value="%d{yyy-MM-dd HH\:mm\:ss,SSS} %p %c{1}(%L) - %m%n"/>
    </layout>
</appender>
<logger name="RocketmqClient" additivity="false">
    <appender-ref ref="RocketmqClientAppender"/>
</logger>
<logger name="RocketmqCommon" additivity="false">
    <appender-ref ref="RocketmqClientAppender"/>
</logger>
<logger name="RocketmqRemoting" additivity="false">
    <appender-ref ref="RocketmqClientAppender"/>
</logger>
```

## 三、<span id="log4j2">日志配置-log4j2[可选]</span>

```
<Appenders>
    <RollingFile name="RocketmqClientAppender" fileName="${sys:client.logRoot}/rocketmq_client.log" filePattern="${sys:client.logRoot}/rocketmq_client-%d{yyyy-MM-dd}-%i.log">
        <PatternLayout pattern="%d{yyy-MM-dd HH\:mm\:ss,SSS} %p %c{1}(%L) - %m%n"/>
        <Policies>
            <TimeBasedTriggeringPolicy/>
            <SizeBasedTriggeringPolicy size="1 GB"/>
        </Policies>
        <DefaultRolloverStrategy max="${sys:client.logFileMaxIndex}"/>
    </RollingFile>
</Appenders>
<Loggers>
    <logger name="RocketmqClient" additivity="false">
        <appender-ref ref="RocketmqClientAppender"/>
    </logger>
    <logger name="RocketmqCommon" additivity="false">
        <appender-ref ref="RocketmqClientAppender"/>
    </logger>
    <logger name="RocketmqRemoting" additivity="false">
        <appender-ref ref="RocketmqClientAppender"/>
    </logger>
</Loggers>
```
