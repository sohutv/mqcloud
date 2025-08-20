package com.sohu.tv.mq.cloud.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import com.sohu.tv.mq.cloud.util.FileDeleteUtil;
/**
 * access日志清理任务
 * 
 * @author yongfeigao
 * @date 2019年1月11日
 */
@Deprecated
public class AccessLogCleanTask {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("${server.tomcat.basedir}")
    private String tomcatBaseDir;

    @Value("${server.tomcat.accesslog.directory:logs}")
    private String accessLogDir;
    
    private String pattern = FileDeleteUtil.ACCESS_LOG_PATTERN;
    
    private int nDaysAgo = 10;
    
    private String dir;

    @Scheduled(cron = "3 3 3 * * ?")
    public void deleteAccessLog() {
        String logDir = dir;
        if(logDir == null) {
            logDir = tomcatBaseDir;
            if(!tomcatBaseDir.endsWith("/")) {
                logDir += "/";
            }
            logDir += accessLogDir;
        }
        logger.info("delete task start dir:{} fileRegex:{} nDaysAgo:{} start", logDir, pattern, nDaysAgo);
        FileDeleteUtil.delete(logDir, pattern, nDaysAgo);
    }

    public void setTomcatBaseDir(String tomcatBaseDir) {
        this.tomcatBaseDir = tomcatBaseDir;
    }

    public void setAccessLogDir(String accessLogDir) {
        this.accessLogDir = accessLogDir;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setnDaysAgo(int nDaysAgo) {
        this.nDaysAgo = nDaysAgo;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
