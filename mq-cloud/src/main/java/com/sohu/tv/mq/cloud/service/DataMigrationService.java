package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.DataMigration;
import com.sohu.tv.mq.cloud.bo.PageLog;
import com.sohu.tv.mq.cloud.dao.DataMigrationDao;
import com.sohu.tv.mq.cloud.service.SSHTemplate.DefaultLineProcessor;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHCallback;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHSession;
import com.sohu.tv.mq.cloud.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.sohu.tv.mq.cloud.bo.DataMigration.STATUS_RUNNING;

/**
 * 数据迁移服务
 * 基于rsync实现
 *
 * @author yongfeigao
 * @date 2024年07月09日
 */
@Service
public class DataMigrationService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String RSYNC_LOG = "/tmp/rsync_%s.log";

    public static final String READ_RSYNC_LOG = "sed -n '%s,%sp' " + RSYNC_LOG;

    public static final String RSYNC_LOG_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    @Autowired
    private DataMigrationDao dataMigrationDao;

    @Autowired
    private SSHTemplate sshTemplate;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    /**
     * 添加迁移任务
     *
     * @param dataMigration
     * @return
     */
    public Result<?> addDataMigration(DataMigration dataMigration) {
        // 1.获取源目录大小
        Result<?> fetchResult = fetchDirSize(dataMigration);
        if (fetchResult.isNotOK()) {
            return fetchResult;
        }
        // 2.保存到数据库
        Result<?> saveResult = saveDataMigration(dataMigration);
        if (saveResult.isNotOK()) {
            return saveResult;
        }
        // 3.启动任务
        Result<?> taskResult = startDataMigrationTask(dataMigration);
        if (taskResult.isNotOK()) {
            return taskResult;
        }
        return Result.getOKResult();
    }

    /**
     * 重新运行迁移任务
     *
     * @param id
     * @return
     */
    public Result<?> rerunDataMigration(long id) {
        try {
            return rerunDataMigration(dataMigrationDao.selectById(id));
        } catch (Exception e) {
            logger.error("selectById:{}", id, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 重新运行迁移任务
     *
     * @param dataMigration
     * @return
     */
    public Result<?> rerunDataMigration(DataMigration dataMigration) {
        if (dataMigration.isRunning()) {
            return Result.getResult(Status.PARAM_ERROR).setMessage("task is running");
        }
        dataMigration.setCostTime(0);
        Result<?> taskResult = startDataMigrationTask(dataMigration);
        if (taskResult.isNotOK()) {
            return taskResult;
        }
        return Result.getOKResult();
    }

    /**
     * 检查所有任务
     */
    public int checkAllDataMigrationTask() {
        try {
            List<DataMigration> list = dataMigrationDao.selectByStatus(STATUS_RUNNING);
            for (DataMigration dataMigration : list) {
                if (isDataMigrationFinished(dataMigration)) {
                    dataMigration.setFinish();
                    updateDataMigration(dataMigration);
                }
            }
            return list.size();
        } catch (Exception e) {
            logger.error("checkAllDataMigrationTask error", e);
        }
        return 0;
    }

    /**
     * 是否任务完成
     *
     * @param dataMigration
     * @return
     */
    public boolean isDataMigrationFinished(DataMigration dataMigration) {
        try {
            SSHResult sshResult = sshTemplate.execute(dataMigration.getSourceIp(), new SSHCallback() {
                public SSHResult call(SSHSession session) {
                    String command = "sudo ps -ef | grep rsync | grep -v 'grep' | grep '" + dataMigration.getDestIp() + "'";
                    return session.executeCommand(command);
                }
            });
            return sshResult != null && sshResult.isSuccess() && StringUtils.isEmpty(sshResult.getResult());
        } catch (SSHException e) {
            logger.error("isTaskFinished, dataMigration:{}", dataMigration, e);
            return false;
        }
    }

    /**
     * 空运行迁移任务
     */
    public Result<?> dryRunDataMigrationTask(DataMigration dataMigration) {
        String command = buildRsyncCommand(dataMigration);
        return Result.getResult(command);
    }

    /**
     * 启动迁移任务
     *
     * @param dataMigration
     * @return
     */
    public Result<?> startDataMigrationTask(DataMigration dataMigration) {
        String command = buildRsyncCommand(dataMigration);
        try {
            SSHResult sshResult = sshTemplate.execute(dataMigration.getSourceIp(), session -> {
                return session.executeCommand(command);
            });
            if (sshResult.isSuccess()) {
                dataMigration.setRunning();
            } else {
                dataMigration.setFinish("run [" + command + "] error:" + sshResult.getResult());
            }
        } catch (Throwable e) {
            logger.error("add task, dataMigration:{} error", dataMigration, e);
            dataMigration.setFinish("add [" + command + "] error:" + e.getMessage());
        }
        return updateDataMigration(dataMigration);
    }

    /**
     * 构建rsync命令
     */
    public String buildRsyncCommand(DataMigration dataMigration) {
        StringBuilder sb = new StringBuilder();
        // 密码
        if (StringUtils.isNotEmpty(mqCloudConfigHelper.getRsyncPassword())) {
            sb.append("env");
            sb.append(" ");
            sb.append("RSYNC_PASSWORD=");
            sb.append(mqCloudConfigHelper.getRsyncPassword());
            sb.append(" ");
        }
        // 后台执行
        sb.append("nohup");
        sb.append(" ");
        if (StringUtils.isNotEmpty(mqCloudConfigHelper.getRsyncPath())) {
            sb.append(mqCloudConfigHelper.getRsyncPath());
            if (!mqCloudConfigHelper.getRsyncPath().endsWith("/")) {
                sb.append("/");
            }
        }
        // rsync命令
        sb.append("rsync -ar --delete --log-file-format='%o /%f %l' --log-file=");
        String rsyncLog = null;
        if (dataMigration.getId() > 0) {
            rsyncLog = String.format(RSYNC_LOG, dataMigration.getId());
        } else {
            rsyncLog = RSYNC_LOG;
        }
        sb.append(rsyncLog);
        // 带宽限制
        if (StringUtils.isNotEmpty(mqCloudConfigHelper.getRsyncBwlimit())) {
            sb.append(" ");
            sb.append("--bwlimit=");
            sb.append(mqCloudConfigHelper.getRsyncBwlimit());
        }
        // 端口
        if (StringUtils.isNotEmpty(mqCloudConfigHelper.getRsyncPort())) {
            sb.append(" ");
            sb.append("--port=");
            sb.append(mqCloudConfigHelper.getRsyncPort());
        }
        // 源目录
        sb.append(" ");
        sb.append(dataMigration.getSourcePath());
        sb.append(" ");
        // 传输用户
        if (StringUtils.isNotEmpty(mqCloudConfigHelper.getRsyncUser())) {
            sb.append(mqCloudConfigHelper.getRsyncUser());
            sb.append("@");
        }
        // 目标ip
        sb.append(dataMigration.getDestIp());
        sb.append("::");
        sb.append(mqCloudConfigHelper.getRsyncModule());
        // 目标目录
        if (!dataMigration.getDestPath().startsWith("/")) {
            sb.append("/");
        }
        sb.append(dataMigration.getDestPath());
        // 日志
        sb.append(" > ");
        sb.append(rsyncLog);
        sb.append(" 2>&1 &");
        return sb.toString();
    }

    /**
     * 获取目录大小
     *
     * @param dataMigration
     * @return
     */
    public Result<?> fetchDirSize(DataMigration dataMigration) {
        try {
            SSHResult sshResult = sshTemplate.execute(dataMigration.getSourceIp(), session -> session.executeCommand("du -sb " + dataMigration.getSourcePath()));
            String result = sshResult.getResult();
            if (result == null) {
                return Result.getResult(Status.NO_RESULT).setMessage("source dir size is null");
            }
            dataMigration.setDataCount(NumberUtils.toLong(result.split("\\s")[0]));
            return Result.getOKResult();
        } catch (SSHException e) {
            logger.error("du, dataMigration:{} error", dataMigration, e);
            return Result.getWebErrorResult(e);
        }
    }

    /**
     * 保存迁移任务
     *
     * @param dataMigration
     * @return
     */
    public Result<?> saveDataMigration(DataMigration dataMigration) {
        try {
            return Result.getResult(dataMigrationDao.insert(dataMigration));
        } catch (Exception e) {
            logger.error("save err, dataMigration:{}", dataMigration, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询迁移任务
     *
     * @return
     */
    public Result<List<DataMigration>> queryAllDataMigration() {
        try {
            return Result.getResult(dataMigrationDao.selectAll());
        } catch (Exception e) {
            logger.error("query err", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 更新迁移任务
     *
     * @param dataMigration
     * @return
     */
    public Result<?> updateDataMigration(DataMigration dataMigration) {
        try {
            return Result.getResult(dataMigrationDao.update(dataMigration));
        } catch (Exception e) {
            logger.error("update err, dataMigration:{}", dataMigration, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 监控日志文件
     */
    public PageLog tailLog(long id, String ip, int offset, int size) {
        PageLog pageLog = new PageLog();
        pageLog.setOffset(offset);
        pageLog.setNextOffset(offset);
        pageLog.setSize(size);
        int startRow = offset;
        // 需要往上读一行，用于解析发送耗时
        if (startRow > 1) {
            startRow -= 1;
        }
        int endRow = startRow + size - 1;
        String command = String.format(READ_RSYNC_LOG, startRow, endRow, id);
        try {
            SSHResult sshResult = sshTemplate.execute(ip, session -> {
                return session.executeCommand(command, new DefaultLineProcessor() {
                    public void process(String line, int lineNum) {
                        pageLog.addContent(line);
                    }
                });
            });
            if (sshResult != null && !sshResult.isSuccess()) {
                pageLog.setError(sshResult.getResult());
            } else if (pageLog.getContentSize() > 0) {
                processLog(pageLog);
            }
        } catch (SSHException e) {
            logger.error("tailLog, id:{}, ip:{}, offset:{}, size:{}", id, ip, offset, size, e);
            pageLog.setError(e.getMessage());
        }
        return pageLog;
    }

    /**
     * 处理日志
     * 主要将发送字节解析成可读的，以及计算发送用时（根据上一行日志的时间差）
     * 格式：2024/07/05 15:47:38 [44488] send /opt/mqcloud/broker-a/data size=4096
     * 处理后：2024/07/05 15:47:38 send /opt/mqcloud/broker-a/data 4096(4KB) 0.1s
     *
     * @param pageLog
     */
    private void processLog(PageLog pageLog) {
        List<String> newContent = new ArrayList<>();
        for (int i = 0; i < pageLog.getContent().size(); ++i) {
            // 多查的第一行不处理
            if (pageLog.getOffset() > 1 && i == 0) {
                continue;
            }
            String row = pageLog.getContent().get(i);
            StringBuilder newLog = new StringBuilder(row);
            long costTime = 0;
            if (i > 0) {
                // 根据上一行时间戳和本行时间戳 计算耗时
                String lastRow = pageLog.getContent().get(i - 1);
                Date lastRowTime = parseDate(lastRow);
                Date thisRowTime = parseDate(row);
                if (lastRowTime != null && thisRowTime != null) {
                    costTime = thisRowTime.getTime() - lastRowTime.getTime();
                }
            }
            // 格式化size
            String[] array = row.split(" ");
            long size = 0;
            if (array.length > 4 && "sent".equals(array[3])) {// 解析最后一行
                size = NumberUtils.toLong(array[4]);
            } else {
                size = NumberUtils.toLong(array[array.length - 1]);
            }
            if (size > 0) {
                newLog.append("<span class='text-muted'>(大小:");
                newLog.append(WebUtil.sizeFormat(size));
                if (costTime > 0) {
                    // 计算传输速率
                    long rate = (long) (size / (costTime / 1000d));
                    if (rate > 0) {
                        newLog.append(" 速率:");
                        newLog.append(WebUtil.sizeFormat(rate));
                        newLog.append("/s");
                    }
                }
                newLog.append(")</span>");
            }
            newContent.add(newLog.toString());
        }
        if (newContent.size() == 0) {
            pageLog.setContent(null);
            return;
        }
        pageLog.setContent(newContent);
        pageLog.setNextOffset(pageLog.getOffset() + pageLog.getContentSize());
    }

    private Date parseDate(String line) {
        String[] array = line.split(" \\[");
        if (array.length != 2) {
            return null;
        }
        try {
            return DateUtil.getFormat(RSYNC_LOG_DATE_FORMAT).parse(array[0]);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 根据id查询
     */
    public Result<DataMigration> query(long id) {
        try {
            return Result.getResult(dataMigrationDao.selectById(id));
        } catch (Exception e) {
            logger.error("query:{}", id, e);
            return Result.getDBErrorResult(e);
        }
    }
}
