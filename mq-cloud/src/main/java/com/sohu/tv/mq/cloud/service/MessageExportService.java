package com.sohu.tv.mq.cloud.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.dao.MessageExportDao;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.rocketmq.consumer.NORebalanceDefaultMQPullConsumer;
import com.sohu.tv.mq.serializable.MessageSerializerEnum;
import com.sohu.tv.mq.util.CommonUtil;
import com.sohu.tv.mq.util.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.MQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.sohu.tv.mq.cloud.bo.Audit.StatusEnum.AUDITING;
import static com.sohu.tv.mq.cloud.bo.Audit.StatusEnum.INIT;

/**
 * 消息导出
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/25
 */
@Service
public class MessageExportService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NameServerService nameServerService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private SSHTemplate sshTemplate;

    @Autowired
    private AuditService auditService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private MessageExportDao messageExportDao;

    @Autowired
    private MessageService messageService;

    private ThreadPoolExecutor exportMessageThreadPool = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("exportMessage-%d").setDaemon(true).build());

    /**
     * 异步导出消息
     *
     * @param topic
     * @param start
     * @param end
     * @param callback
     */
    public void exportAsync(long aid, String topic, long start, long end, java.util.function.Consumer<Result> callback) {
        exportMessageThreadPool.execute(() -> {
            try {
                // 更新状态为审核中
                Result<Integer> countResult = auditService.updateAuditStatus(aid, AUDITING.getStatus(), INIT.getStatus());
                if (countResult.isNotOK() || countResult.getResult() == 0) {
                    logger.error("update audit status error, audit:{}, result:{}", aid, countResult);
                    return;
                }
                // 保存消息导出记录
                saveRecord(aid);
                // 消息导出
                Result<?> exportResult = export(aid, topic, start, end);
                // 回调
                callback.accept(exportResult);
            } catch (Exception e) {
                logger.error("exportMessage error, topic:{}, start:{}, end:{}", topic, start, end, e);
                updateRecord(aid, e.getMessage());
            }
        });
    }

    /**
     * 消息导出
     *
     * @param topic
     * @param start
     * @param end
     * @return
     */
    public Result<?> export(long aid, String topic, long start, long end) {
        // 获取topic信息
        boolean isDLQ = CommonUtil.isDeadTopic(topic);
        Result<Topic> topicResult = null;
        if (isDLQ) {
            String consumer = topic.substring(MixAll.DLQ_GROUP_TOPIC_PREFIX.length());
            Result<Consumer> consResult = consumerService.queryConsumerByName(consumer);
            if (consResult.isNotOK()) {
                updateRecord(aid, "consumer:" + consumer + "不存在");
                return consResult;
            }
            topicResult = topicService.queryTopic(consResult.getResult().getTid());
        } else {
            topicResult = topicService.queryTopic(topic);
        }
        if (topicResult.isNotOK()) {
            updateRecord(aid, "topic:" + topic + "不存在");
            return topicResult;
        }
        int serializerType = topicResult.getResult().getSerializer();
        // 查询nameserver信息
        Result<List<NameServer>> nsListResult = nameServerService.query((int) topicResult.getResult().getClusterId());
        if (nsListResult.isNotOK()) {
            updateRecord(aid, "ns不存在");
            return nsListResult;
        }
        SimpleDateFormat sdf = DateUtil.getFormat(DateUtil.YMDHMS);
        String startDate = sdf.format(new Date(start));
        String endDate = sdf.format(new Date(end));
        logger.info("topic:{} time[{},{}] export begin", topic, startDate, endDate);
        BufferedWriter writer = null;
        File file = null;
        MQPullConsumer pullConsumer = null;
        long exportedCount = 0;
        long time = System.currentTimeMillis();
        try {
            // 特定类型使用自定义的classloader
            if (mqCloudConfigHelper.getClassList() != null &&
                    mqCloudConfigHelper.getClassList().contains(topicResult.getResult().getName())) {
                Thread.currentThread().setContextClassLoader(messageService.getMessageTypeClassLoader());
            }
            pullConsumer = buildMQPullConsumer(topic, nsListResult.getResult().stream()
                    .map(NameServer::getAddr)
                    .collect(Collectors.joining(";")), start, end);
            // 获取消息队列
            Set<MessageQueue> mqs = pullConsumer.fetchSubscribeMessageQueues(topic);
            if (mqs == null || mqs.size() == 0) {
                logger.warn("{}'s messageQueue is empty!", topic);
                updateRecord(aid, "topic:" + topic + "的队列为空");
                return Result.getResult(Status.NO_RESULT);
            }
            String localPath = mqCloudConfigHelper.getExportedMessageLocalPath();
            if (StringUtils.isBlank(localPath)) {
                ApplicationHome home = new ApplicationHome(getClass());
                localPath = home.getDir().getAbsolutePath();
            }
            file = new File(localPath, topic + "-" + startDate + "-" + endDate);
            writer = Files.newBufferedWriter(Paths.get(file.toURI()));
            // 获取队列偏移量
            List<MQOffset> mqOffsetList = getMQOffsetList(mqs, pullConsumer, start, end);
            // 记录总数
            long totalCount = getTotalMessageCount(mqOffsetList);
            recordTotalMessageCount(aid, totalCount);
            // 遍历队列
            for (MQOffset mqOffset : mqOffsetList) {
                logger.info("mqOffset:{}", mqOffset);
                long startOffset = mqOffset.getMinOffset();
                long endOffset = mqOffset.getMaxOffset();
                long prevExportedCount = exportedCount;
                MessageQueue mq = mqOffset.getMq();
                // 拉取消息
                while (startOffset < endOffset) {
                    PullResult pullResult = pullConsumer.pull(mq, "*", startOffset, 32);
                    // 防止offset不前进
                    if (startOffset < pullResult.getNextBeginOffset()) {
                        startOffset = pullResult.getNextBeginOffset();
                    } else {
                        ++startOffset;
                    }
                    // 无消息继续
                    if (PullStatus.FOUND != pullResult.getPullStatus()) {
                        continue;
                    }
                    // 时间过滤
                    for (MessageExt msg : pullResult.getMsgFoundList()) {
                        long msgTime = msg.getBornTimestamp();
                        if (isDLQ) {
                            msgTime = msg.getStoreTimestamp();
                        }
                        if (msgTime >= start && msgTime <= end) {
                            ++exportedCount;
                            if (logger.isDebugEnabled()) {
                                logger.info("fetch msgId:{} from:{}:{} time:{}", msg.getMsgId(),
                                        mq.getBrokerName(), mq.getQueueId(), sdf.format(new Date(msgTime)));
                            }
                            // 写入文件
                            writer.append(decodeMessage(serializerType, msg));
                            writer.newLine();
                        }
                    }
                    if (prevExportedCount == 0 || exportedCount - prevExportedCount >= 50000) {
                        prevExportedCount = exportedCount;
                        recordExportedMessageCount(aid, totalCount, exportedCount, System.currentTimeMillis() - time,
                                "正在导出" + mq.getBrokerName() + ":" + mq.getQueueId());
                    }
                }
                // 记录导出消息量
                recordExportedMessageCount(aid, totalCount, exportedCount, System.currentTimeMillis() - time,
                        mq.getBrokerName() + ":" + mq.getQueueId() + "导出完成");
            }
            long use = System.currentTimeMillis() - time;
            logger.info("export:{} time[{},{}] size:{} export use:{}ms", file.getAbsolutePath(), startDate, endDate, exportedCount, use);
            updateRecord(aid, use, file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("{} export start:{} end:{} error", topic, startDate, endDate, e);
            updateRecord(aid, "export:" + e.getMessage());
            return Result.getDBErrorResult(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (pullConsumer != null) {
                pullConsumer.shutdown();
            }
        }
        if (exportedCount == 0) {
            return Result.getOKResult();
        }
        // 复制文件到下载机器
        return copyFileToDownloadMachine(aid, file);
    }

    private String decodeMessage(int serializerType, MessageExt msg) throws Exception {
        Object msgObj = MessageSerializerEnum.getMessageSerializerByType(serializerType).deserialize(msg.getBody());
        String msgString = null;
        if (msgObj instanceof byte[]) {
            msgString = new String((byte[]) msgObj, "UTF-8");
        } else if (msgObj instanceof String) {
            msgString = (String) msgObj;
        } else {
            msgString = JSONUtil.toJSONString(msgObj);
        }
        ExportedMessage exportedMessage = new ExportedMessage(msg.getMsgId(), msg.getStoreTimestamp(), msgString);
        return JSONUtil.toJSONString(exportedMessage);
    }

    /**
     * 保存记录
     * @param aid
     */
    private void saveRecord(long aid) {
        MessageExport messageExport = new MessageExport();
        messageExport.setAid(aid);
        messageExport.setIp(CommonUtil.IP);
        messageExport.setInfo("导出任务开始");
        messageExportDao.insert(messageExport);
    }

    /**
     * 更新记录
     * @param aid
     */
    private void updateRecord(long aid, String info) {
        MessageExport messageExport = new MessageExport();
        messageExport.setAid(aid);
        messageExport.setInfo(info);
        messageExportDao.update(messageExport);
    }

    private void updateRecord(long aid, long exporCostTime, String exportedFilePath) {
        MessageExport messageExport = new MessageExport();
        messageExport.setAid(aid);
        messageExport.setExportCostTime(exporCostTime);
        messageExport.setExportedFilePath(exportedFilePath);
        messageExport.setInfo("导出到本地完成");
        messageExportDao.update(messageExport);
    }

    private void updateCompressCostTime(long aid, long compressCostTime, String info) {
        MessageExport messageExport = new MessageExport();
        messageExport.setAid(aid);
        messageExport.setCompressCostTime(compressCostTime);
        messageExport.setInfo(info);
        messageExportDao.update(messageExport);
    }

    private void updateScpCostTime(long aid, long scpCostTime, String info) {
        MessageExport messageExport = new MessageExport();
        messageExport.setAid(aid);
        messageExport.setScpCostTime(scpCostTime);
        messageExport.setInfo(info);
        messageExportDao.update(messageExport);
    }

    /**
     * 获取总消息量
     *
     * @param mqOffsetList
     * @return
     */
    private long getTotalMessageCount(List<MQOffset> mqOffsetList) {
        return mqOffsetList.stream().mapToLong(mqOffset -> {
            return mqOffset.getMaxOffset() - mqOffset.getMinOffset();
        }).sum();
    }

    /**
     * 记录总消息量
     *
     * @param mqOffsetList
     * @param aid
     */
    private void recordTotalMessageCount(long aid, long count) {
        if (aid == 0) {
            return;
        }
        MessageExport messageExport = new MessageExport();
        messageExport.setAid(aid);
        messageExport.setTotalMsgCount(count);
        messageExportDao.update(messageExport);
    }

    /**
     * 记录导出的消息量
     *
     * @param mqOffsetList
     * @param aid
     */
    private void recordExportedMessageCount(long aid, long totalCount, long exportedCount, long use, String info) {
        if (aid == 0) {
            return;
        }
        long leftTime = 0;
        if (exportedCount != 0) {
            leftTime = (long) ((double) use * (totalCount - exportedCount) / exportedCount);
        }
        MessageExport messageExport = new MessageExport();
        messageExport.setAid(aid);
        messageExport.setExportedMsgCount(exportedCount);
        messageExport.setLeftTime(leftTime);
        messageExport.setInfo(info);
        messageExportDao.update(messageExport);
    }

    /**
     * 获取消息队列偏移量
     *
     * @param mqs
     * @param pullConsumer
     * @param start
     * @param end
     * @return
     * @throws MQClientException
     */
    private List<MQOffset> getMQOffsetList(Set<MessageQueue> mqs, MQPullConsumer pullConsumer, long start, long end) throws MQClientException {
        List<MQOffset> offsetList = new ArrayList<MQOffset>();
        for (MessageQueue mq : mqs) {
            // 获取偏移量
            long endOffset = pullConsumer.searchOffset(mq, end);
            long startOffset = pullConsumer.searchOffset(mq, start);
            // 处理非法情况
            if (startOffset >= endOffset) {
                if (startOffset == 0) {
                    endOffset = 1;
                } else {
                    endOffset = startOffset + 1;
                }
            }
            // 拼装offset
            MQOffset mqOffset = new MQOffset();
            mqOffset.setMq(mq);
            mqOffset.setMaxOffset(endOffset);
            mqOffset.setMinOffset(startOffset);
            offsetList.add(mqOffset);
        }
        return offsetList;
    }

    /**
     * 构建PullConsumer
     *
     * @param topic
     * @param nAddr
     * @param start
     * @param end
     * @return
     * @throws MQClientException
     */
    public MQPullConsumer buildMQPullConsumer(String topic, String nAddr, long start, long end) throws MQClientException {
        // 初始化消费者
        DefaultMQPullConsumer pullConsumer = new DefaultMQPullConsumer(MixAll.CID_RMQ_SYS_PREFIX + topic);
        pullConsumer.setNamesrvAddr(nAddr);
        // 开始时间和结束时间作为实例id
        String instance = start + "@" + end;
        pullConsumer.setInstanceName(instance);
        try {
            // 为DefaultMQPullConsumer赋予NORebalanceDefaultMQPullConsumer
            Field field = DefaultMQPullConsumer.class.getDeclaredField("defaultMQPullConsumerImpl");
            field.setAccessible(true);
            field.set(pullConsumer, new NORebalanceDefaultMQPullConsumer(pullConsumer, null));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        // 启动
        pullConsumer.start();
        // 从slave拉取
        pullConsumer.getDefaultMQPullConsumerImpl().getPullAPIWrapper().setConnectBrokerByUser(true);
        pullConsumer.getDefaultMQPullConsumerImpl().getPullAPIWrapper().setDefaultBrokerId(MixAll.MASTER_ID + 1);
        return pullConsumer;
    }

    /**
     * 复制文件到下载机器
     *
     * @param file
     * @return
     */
    public Result<?> copyFileToDownloadMachine(long aid, File file) {
        // 首先压缩文件
        try {
            file = compressFile(aid, file);
        } catch (IOException e) {
            logger.error("compress {} error", file.getName(), e);
        }
        String exportedMessageRemotePath = mqCloudConfigHelper.getExportedMessageRemotePath();
        if (exportedMessageRemotePath == null) {
            return Result.getOKResult();
        }
        long start = System.currentTimeMillis();
        String[] arrays = exportedMessageRemotePath.split(":");
        final File finalFile = file;
        try {
            SSHTemplate.SSHResult scpRst = sshTemplate.execute(arrays[0], new SSHTemplate.SSHCallback() {
                public SSHTemplate.SSHResult call(SSHTemplate.SSHSession session) {
                    String remotePath = arrays[1];
                    if (!remotePath.endsWith("/")) {
                        remotePath += "/";
                    }
                    return session.scpToFile(finalFile.getAbsolutePath(), remotePath + finalFile.getName());
                }
            });
            if (!scpRst.isSuccess()) {
                logger.error("scp {} err", file.getName(), scpRst.getExcetion());
                updateRecord(aid, "scp error:" + scpRst.getExcetion().getMessage());
                return Result.getRequestErrorResult(scpRst.getExcetion()).setMessage("scp err");
            }
            long cost = System.currentTimeMillis() - start;
            logger.info("scp {} to {} use:{}ms", file.getName(), exportedMessageRemotePath, cost);
            updateScpCostTime(aid, cost, "已传输到" + exportedMessageRemotePath);
            if (mqCloudConfigHelper.getExportedMessageDownloadUrlPrefix() != null) {
                String fileName = file.getName();
                try {
                    fileName = URLEncoder.encode(fileName, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                }
                return Result.getOKResult().setResult(mqCloudConfigHelper.getExportedMessageDownloadUrlPrefix() + fileName);
            }
        } catch (Exception e) {
            logger.error("scp {} error", file.getName(), e);
            updateRecord(aid, "scp err:" + e.getMessage());
            return Result.getRequestErrorResult(e).setMessage("scp error");
        }
        return Result.getOKResult();
    }

    /**
     * zip压缩文件
     *
     * @param file
     * @return
     * @throws IOException
     */
    public File compressFile(long aid, File file) throws IOException {
        long start = System.currentTimeMillis();
        File zipFile = new File(file.getAbsolutePath() + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);
            Files.copy(file.toPath(), zos);
            zos.closeEntry();
        }
        long cost = System.currentTimeMillis() - start;
        logger.info("compress {} to {} use:{}ms", file.getName(), zipFile.getName(), System.currentTimeMillis() - start);
        file.delete();
        updateCompressCostTime(aid, cost, "压缩完成");
        return zipFile;
    }

    public Result<MessageExport> getMessageExport(long aid) {
        MessageExport messageExport = null;
        try {
            messageExport = messageExportDao.select(aid);
        } catch (Exception e) {
            logger.error("select:{} err", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(messageExport);
    }

    public Result<Integer> deleteMessageExport(long aid) {
        try {
            return Result.getResult(messageExportDao.delete(aid));
        } catch (Exception e) {
            logger.error("delete:{} err", aid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询导出任务执行超过time未更新的
     * @return
     */
    public Result<List<MessageExport>> getMessageExportLaterThan(Date time) {
        try {
            return Result.getResult(messageExportDao.selectLaterThan(time));
        } catch (Exception e) {
            logger.error("selectLaterThan:{} err", time, e);
            return Result.getDBErrorResult(e);
        }
    }
}
