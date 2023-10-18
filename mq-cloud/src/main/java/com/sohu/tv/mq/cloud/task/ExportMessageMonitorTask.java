package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.bo.MessageExport;
import com.sohu.tv.mq.cloud.bo.UserWarn;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.MessageExportService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息导出监控任务
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/26
 */
public class ExportMessageMonitorTask {

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private MessageExportService messageExportService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Scheduled(cron = "11 */5 * * * *")
    @SchedulerLock(name = "exportMessageMonitor", lockAtMostFor = 180000, lockAtLeastFor = 180000)
    public void exportMessageMonitor() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                Date time = new Date(System.currentTimeMillis() - 10 * 60 * 1000);
                Result<List<MessageExport>> listResult = messageExportService.getMessageExportLaterThan(time);
                if (listResult.isEmpty()) {
                    return;
                }
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("list", listResult.getResult());
                paramMap.put("linkPrefix", mqCloudConfigHelper.getPrefix());
                alertService.sendWarn(null, UserWarn.WarnType.MESSAGE_EXPORT_ERROR, paramMap);
            }
        });
    }
}
