package com.sohu.tv.mq.cloud.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 文件删除工具
 * 
 * @author yongfeigao
 * @date 2019年1月10日
 */
public class FileDeleteUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(FileDeleteUtil.class);
    
    // 形如*log.yyyy-mm-dd*
    public static final String ACCESS_LOG_PATTERN = ".*log\\.\\d{4}-\\d{2}-\\d{2}.*";
    
    /**
     * 删除dir下的fileRegex格式的maxDays前的文件
     * @param dir 绝对路径
     * @param fileRegex 文件正则表达式
     * @param nDaysAgo n天前
     */
    public static void delete(String dir, String fileRegex, int nDaysAgo) {
        logger.info("begin delete dir:{}", dir);
        try {
            File logDir = new File(dir);
            if(!logDir.exists()) {
                logger.warn("dir:{} not exists", dir);
                return;
            }
            File[] files = logDir.listFiles();
            if(files == null) {
                logger.warn("dir:{} no files", dir);
                return;
            }
            Pattern filePattern = Pattern.compile(fileRegex);
            long time = System.currentTimeMillis() - nDaysAgo * 24 * 60 * 60 * 1000L;
            for(File file : files) {
                // 目录递归删除
                if(file.isDirectory()) {
                    delete(file.getAbsolutePath(), fileRegex, nDaysAgo);
                    continue;
                }
                String fileName = file.getName();
                Matcher matcher = filePattern.matcher(fileName);
                if(!matcher.matches()) {
                    continue;
                }
                if(file.lastModified() < time) {
                    boolean result = file.delete();
                    logger.info("delete file:{} result:{}", file.getAbsolutePath(), result);
                }
            }
        } catch (Exception e) {
            logger.error("delete dir:{} fileRegex:{} nDaysAgo:{} err", dir, fileRegex, nDaysAgo, e);
        }
    }
}
