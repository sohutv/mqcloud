package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdate;
import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.service.action.BrokerActionChooser;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action.*;

/**
 * 集群broker自动更新服务
 *
 * @author yongfeigao
 * @date 2024年11月04日
 */
@Service
public class ClusterBrokerAutoUpdateService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BrokerAutoUpdateService brokerAutoUpdateService;

    @Autowired
    private BrokerAutoUpdateStepService brokerAutoUpdateStepService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private LockProvider lockProvider;

    @Autowired
    private BrokerActionChooser brokerActionChooser;

    @Autowired
    private AlarmConfigBridingService alarmConfigBridingService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    /**
     * 保存自动更新
     */
    public Result<?> save(int cid) {
        // 查询broker
        Result<List<Broker>> brokerListResult = brokerService.query(cid);
        if (brokerListResult.isEmpty()) {
            return brokerListResult;
        }
        // 获取锁
        Optional<SimpleLock> lockOptional = lockProvider.lock(new LockConfiguration("autoUpdate-" + cid,
                Instant.now().plusSeconds(5)));
        if (!lockOptional.isPresent()) {
            return Result.getResult(Status.DB_ERROR).setMessage("获取锁失败");
        }
        try {
            // 查询未完成的任务
            Result<List<BrokerAutoUpdate>> undoneResult = brokerAutoUpdateService.selectUndoneByCid(cid);
            if (undoneResult.isNotOK()) {
                return undoneResult;
            }
            if (undoneResult.isNotEmpty()) {
                return Result.getResult(Status.DB_ERROR).setMessage("存在未完成的任务");
            }
            // 保存BrokerAutoUpdate
            Result<?> result = brokerAutoUpdateService.save(cid, toBrokerAutoUpdateStep(brokerListResult.getResult()));
            if (result.isNotOK()) {
                return result;
            }
            return Result.getOKResult();
        } finally {
            lockOptional.get().unlock();
        }
    }

    /**
     * 分组broker
     */
    private List<BrokerAutoUpdateStep> toBrokerAutoUpdateStep(List<Broker> brokerList) {
        Map<String, List<Broker>> brokerMap = groupBroker(brokerList);
        // 转换为BrokerAutoUpdateStep
        List<BrokerAutoUpdateStep> steps = new ArrayList<>();
        for (List<Broker> brokerGroup : brokerMap.values()) {
            // 如果只有一个broker, 则可以直接更新
            if (brokerGroup.size() == 1) {
                addSingleBrokerUpdateStep(steps, brokerGroup.get(0));
            } else {
                // 如果全部是slave，则直接更新
                if (!brokerGroup.get(0).isMaster()) {
                    for (Broker broker : brokerGroup) {
                        addCommonBrokerUpdateStep(steps, broker);
                    }
                } else {
                    addBrokerUpdateStepWithMaster(steps, brokerGroup);
                }
            }
        }
        return steps;
    }

    /**
     * 添加有master的broker的更新步骤
     */
    private void addBrokerUpdateStepWithMaster(List<BrokerAutoUpdateStep> steps, List<Broker> brokers) {
        Broker master = brokers.get(0);
        // master停写
        steps.add(BrokerAutoUpdateStep.build(steps.size(), master, STOP_WRITE));
        // 更新所有slave
        for (int i = 1; i < brokers.size(); ++i) {
            addCommonBrokerUpdateStep(steps, brokers.get(i));
        }
        // master取消注册
        steps.add(BrokerAutoUpdateStep.build(steps.size(), master, UNREGISTER));
        // master更新
        addCommonBrokerUpdateStep(steps, master);
        // master注册
        steps.add(BrokerAutoUpdateStep.build(steps.size(), master, REGISTER));
        // master恢复写入
        steps.add(BrokerAutoUpdateStep.build(steps.size(), master, RECOVER_WRITE));
    }

    /**
     * 添加单个broker的更新步骤
     */
    private void addSingleBrokerUpdateStep(List<BrokerAutoUpdateStep> steps, Broker broker) {
        if (broker.isMaster()) {
            steps.add(BrokerAutoUpdateStep.build(steps.size(), broker, STOP_WRITE));
        }
        addCommonBrokerUpdateStep(steps, broker);
        if (broker.isMaster()) {
            steps.add(BrokerAutoUpdateStep.build(steps.size(), broker, RECOVER_WRITE));
        }
    }

    /**
     * 添加通用的broker的更新步骤
     */
    private void addCommonBrokerUpdateStep(List<BrokerAutoUpdateStep> steps, Broker broker) {
        steps.add(BrokerAutoUpdateStep.build(steps.size(), broker, SHUTDOWN));
        steps.add(BrokerAutoUpdateStep.build(steps.size(), broker, BACKUP_DATA));
        steps.add(BrokerAutoUpdateStep.build(steps.size(), broker, DOWNLOAD));
        steps.add(BrokerAutoUpdateStep.build(steps.size(), broker, UNZIP));
        steps.add(BrokerAutoUpdateStep.build(steps.size(), broker, RECOVER_DATA));
        steps.add(BrokerAutoUpdateStep.build(steps.size(), broker, START));
    }

    /**
     * 分组broker
     */
    private Map<String, List<Broker>> groupBroker(List<Broker> brokerList) {
        Map<String, List<Broker>> brokerMap = new TreeMap<>();
        for (Broker broker : brokerList) {
            brokerMap.computeIfAbsent(broker.getBrokerName(), k -> new ArrayList<>()).add(broker);
        }
        // 每组broker按照brokerID排序
        brokerMap.values().forEach(brokers -> brokers.sort(Comparator.comparing(Broker::getBrokerID)));
        return brokerMap;
    }

    /**
     * 执行自动更新
     */
    public Result<?> autoUpdate() {
        // 查询可执行的任务
        List<BrokerAutoUpdate> list = brokerAutoUpdateService.selectExecutable().getResult();
        if (list == null || list.isEmpty()) {
            return Result.getOKResult();
        }
        // 检查是否有重复的cid，一个集群只能有一个可执行的任务
        boolean duplicate = list.stream()
                .map(BrokerAutoUpdate::getCid)
                .collect(Collectors.toSet())
                .size() < list.size();
        if (duplicate) {
            logger.error("duplicate cid:{}", list);
            return Result.getResult(Status.DB_DUPLICATE_KEY).setMessage("存在重复的cid");
        }
        // 随机打乱
        Collections.shuffle(list);
        return execute(list.get(0));
    }

    /**
     * 执行任务：更新BrokerAutoUpdate
     */
    private Result<?> execute(BrokerAutoUpdate brokerAutoUpdate) {
        // 每个任务最多锁定30分钟
        Optional<SimpleLock> lockOptional = lockProvider.lock(new LockConfiguration(
                "autoUpdateStep-" + brokerAutoUpdate.getCid(), Instant.now().plusSeconds(1800)));
        if (!lockOptional.isPresent()) {
            return Result.getOKResult();
        }
        try {
            Result<BrokerAutoUpdateStep> stepResult = brokerAutoUpdateStepService.selectExecutable(brokerAutoUpdate.getId());
            // 数据库异常跳出
            if (stepResult.getException() != null) {
                return stepResult;
            }
            return execute(brokerAutoUpdate, stepResult.getResult());
        } finally {
            lockOptional.get().unlock();
        }
    }

    /**
     * 执行步骤更新
     */
    private Result<?> execute(BrokerAutoUpdate brokerAutoUpdate, BrokerAutoUpdateStep step) {
        // 如果全部完成，则更新brokerAutoUpdate状态
        if (step == null) {
            brokerAutoUpdate.setStatus(BrokerAutoUpdate.Status.SUCCESS.getValue());
            brokerAutoUpdateService.update(brokerAutoUpdate);
            return Result.getOKResult();
        }
        step.setCid(brokerAutoUpdate.getCid());
        // 执行错误或正在运行的步骤，整体停止执行
        if (step.isFailed() || step.isRunning()) {
            return warnErrorStatus(step);
        }
        // 如果已经准备好，则更新为运行中
        if (brokerAutoUpdate.isReady()) {
            brokerAutoUpdate.setStatus(BrokerAutoUpdate.Status.RUNNING.getValue());
            if (brokerAutoUpdate.getStartTime() == null) {
                brokerAutoUpdate.setStartTime(new Date());
            }
            brokerAutoUpdateService.update(brokerAutoUpdate);
        }
        // 执行步骤
        return brokerActionChooser.choose(step.getActionEnum()).execute(step);
    }

    private void updateBrokerAutoUpdateStep(int id, String info) {
        BrokerAutoUpdateStep update = new BrokerAutoUpdateStep();
        update.setId(id);
        update.setInfo(info);
        brokerAutoUpdateStepService.update(update);
    }

    /**
     * 需要人工修复的错误
     */
    private Result<?> warnErrorStatus(BrokerAutoUpdateStep step) {
        logger.error("[FIX]need human fix error and then reset status:{}", step);
        if (step.getInfo() == null) {
            updateBrokerAutoUpdateStep(step.getId(), step.getStatusEnum().toString());
        }
        // 验证报警频率
        String[] keys = {"autoUpdate", String.valueOf(step.getId()), String.valueOf(step.getStatus())};
        if (alarmConfigBridingService.needWarn(10 * 60 * 1000, keys)) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("brokerNameLink", mqCloudConfigHelper.getBrokerAutoUpdateLink(step.getCid(),
                    step.getBrokerAutoUpdateId(), step.getBrokerName()));
            paramMap.put("step", step);
            alertService.sendWarn(null, WarnType.BROKER_AUTO_UPDATE_WARN, paramMap);
        }
        return Result.getResult(Status.REQUEST_ERROR).setMessage("step:" + step.getId() + " status:" + step.getStatusEnum());
    }
}
