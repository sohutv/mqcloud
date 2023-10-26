package com.sohu.tv.mq.cloud.task.server.nmon;

import com.sohu.tv.mq.cloud.task.server.data.OS;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * nmon文件查询器
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月18日
 */
@Component
public class NMONFileFinder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(NMONFileFinder.class);
    public static final String NMON_PATH = "/nmon";
    public static final String NMON_DIR_PATH = "nmon.dir";
    public static final String FILE = "file";
    // nmon文件存储 key为OSType_ProcessorArchitecture_DistributionType
    private static final Map<String, File> nmonFileMap = new HashMap<String, File>();
    
    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public void run(String... args) throws Exception {
        init();
    }
    
    /**
     * 初始化nmon文件
     */
    private void init() {
        try {
            String path = System.getProperty(NMON_DIR_PATH);
            if (path == null) {
                String classpath = null;
                try {
                    CodeSource codeSource = NMONFileFinder.class.getProtectionDomain().getCodeSource();
                    classpath = codeSource.getLocation().getPath();
                    if (classpath.startsWith(FILE)) {
                        // like that: file:/opt/xxx.war!/WEB-INF/classes!/
                        classpath = classpath.substring(FILE.length() + 1);
                    }
                    // 如果是jar或者war包，取其上一级路径
                    if (new File(classpath).isDirectory()) {
                        path = classpath + "../.." + NMON_PATH;
                    } else {
                        // like that: /opt/xxx.war!/WEB-INF/classes!/
                        String[] tmp = classpath.split("!/", 2);
                        path = tmp[0].substring(0, tmp[0].lastIndexOf("/")) + NMON_PATH;
                    }
                } catch (Exception e) {
                    logger.error(classpath, e);
                }
            }
            File nmonDir = new File(path);
            if (!nmonDir.exists()) {
                // 线上环境尝试通过http加载
                boolean ok = downloadAndUnzip(nmonDir.getParentFile());
                if(!ok) {
                    logger.error("{} path not exist", nmonDir.getAbsolutePath());
                    return;
                }
            }
            // 获取操作系统目录
            File[] osDirs = nmonDir.listFiles();
            if (osDirs == null) {
                logger.error("{} not contains OS folders", nmonDir.getAbsolutePath());
                return;
            }
            for (File osDir : osDirs) {
                // 获取处理器架构目录
                File[] archFiles = osDir.listFiles();
                if (archFiles == null) {
                    logger.info("{} not contains architecture folders", osDir.getName());
                    continue;
                }
                for (File archDir : archFiles) {
                    // 获取nmon文件目录
                    File[] nmonFiles = archDir.listFiles();
                    if (nmonFiles == null) {
                        logger.info("{} not contains nomon files", archDir.getName());
                        continue;
                    }
                    for (File nmonFile : nmonFiles) {
                        nmonFileMap.put(osDir.getName() + "_" + archDir.getName()
                                + "_" + nmonFile.getName(), nmonFile);
                    }
                    logger.info("init {} {} nmon file size=" + nmonFiles.length,
                            osDir.getName(), archDir.getName());
                }
            }
            logger.info("init {} finished, os size={}", nmonDir.getAbsolutePath(), osDirs.length);
        } catch (Exception e) {
            logger.error("init nmon factory", e);
        }
    }

    /**
     * 从远程下载nmon
     * @param nmonDir
     * @return
     */
    protected boolean downloadAndUnzip(File nmonDir) {
        try {
            String path = nmonDir.getAbsolutePath() + File.separatorChar + MQCloudConfigHelper.NMON_ZIP;
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            InputStream inputStream = resourceLoader.getResource("classpath:/static/software/nmon.zip").getInputStream();
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry zipEntry = zis.getNextEntry();
            while(zipEntry != null){
                String fileName = zipEntry.getName();
                File newFile = new File(nmonDir.getAbsolutePath() + File.separatorChar + fileName);
                if(zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            logger.info("download nmon:{} OK!", nmonDir);
        } catch (Exception e) {
            logger.error("download err", e);
            return false;
        }
        return true;
    }

    /**
     * 根据OS信息获取对应版本的NMON文件
     * 
     * @param os
     * @return File
     */
    public File getNMONFile(OS os) {
        String key = os.getOsType().getValue()
                + "_" + os.getProcessorArchitecture().getValue()
                + "_" + os.getDistributionType().getNmonName()
                + os.getDistributionVersion().getNmonVersion();
        return nmonFileMap.get(key);
    }

}
