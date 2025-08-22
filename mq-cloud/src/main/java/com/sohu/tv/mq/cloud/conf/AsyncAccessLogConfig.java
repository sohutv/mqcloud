package com.sohu.tv.mq.cloud.conf;

import org.apache.catalina.valves.AccessLogValve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.CharArrayWriter;

/**
 * 异步访问日志配置
 *
 * @author yongfeigao
 * @date 2025年06月26日
 */
@Configuration
public class AsyncAccessLogConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> asyncAccessLogCustomizer() {
        return factory -> factory.addEngineValves(createAsyncAccessLogValve());
    }

    private AccessLogValve createAsyncAccessLogValve() {
        AccessLogValve valve = new LogbackAsyncAccessLogValve();
        valve.setEnabled(true);
        valve.setPattern("%{yyyy-MM-dd HH:mm:ss.sss}t %I %r %h %s %b %D %F %{X-Forwarded-For}i");
        valve.setBuffered(false);
        return valve;
    }

    class LogbackAsyncAccessLogValve extends AccessLogValve {

        private final Logger logger = LoggerFactory.getLogger("accessLog");

        @Override
        public void log(CharArrayWriter message) {
            String logMessage = message.toString();
            logger.info(logMessage);
        }
    }
}