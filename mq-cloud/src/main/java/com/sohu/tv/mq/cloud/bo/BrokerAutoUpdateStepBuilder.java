package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action;

import java.util.*;

import static com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep.Action.*;

/**
 * broker自动更新步骤构建器
 *
 * @author yongfeigao
 * @date 2025年03月06日
 */
public class BrokerAutoUpdateStepBuilder {
    // 更新操作
    public static final int UPDATE_ACTION = 0;
    // 操作
    private int action;
    // 操作步骤
    private List<BrokerAutoUpdateStep> steps;

    private BrokerAutoUpdateStepBuilder(int action) {
        this.action = action;
        steps = new ArrayList<>();
    }

    /**
     * 构建broker自动更新步骤
     */
    public static List<BrokerAutoUpdateStep> build(List<Broker> brokerList, int action) {
        BrokerAutoUpdateStepBuilder builder = new BrokerAutoUpdateStepBuilder(action);
        Map<String, List<Broker>> brokerMap = builder.groupBroker(brokerList);
        for (List<Broker> brokerGroup : brokerMap.values()) {
            // 如果只有一个broker, 则可以直接更新
            if (brokerGroup.size() == 1) {
                builder.addSingleBrokerUpdateStep(brokerGroup.get(0));
            } else {
                // 如果全部是slave，则直接更新
                if (!brokerGroup.get(0).isMaster()) {
                    for (Broker broker : brokerGroup) {
                        builder.addCommonBrokerUpdateStep(broker);
                    }
                } else {
                    builder.addBrokerUpdateStepWithMaster(brokerGroup);
                }
            }
        }
        return builder.steps;
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
     * 添加单个broker的更新步骤
     */
    private void addSingleBrokerUpdateStep(Broker broker) {
        if (broker.isMaster()) {
            addBrokerAutoUpdateStep(broker, STOP_WRITE);
        }
        addCommonBrokerUpdateStep(broker);
        if (broker.isMaster()) {
            addBrokerAutoUpdateStep(broker, RECOVER_WRITE);
        }
    }

    /**
     * 添加通用的broker的更新步骤
     */
    private void addCommonBrokerUpdateStep(Broker broker) {
        addBrokerAutoUpdateStep(broker, SHUTDOWN);
        // action=0表示更新
        if (action == UPDATE_ACTION) {
            addBrokerAutoUpdateStep(broker, BACKUP_DATA);
            addBrokerAutoUpdateStep(broker, DOWNLOAD);
            addBrokerAutoUpdateStep(broker, UNZIP);
            addBrokerAutoUpdateStep(broker, RECOVER_DATA);
        }
        addBrokerAutoUpdateStep(broker, START);
    }

    /**
     * 添加有master的broker的更新步骤
     */
    private void addBrokerUpdateStepWithMaster(List<Broker> brokers) {
        Broker master = brokers.get(0);
        // master停写
        addBrokerAutoUpdateStep(master, STOP_WRITE);
        // 更新所有slave
        for (int i = 1; i < brokers.size(); ++i) {
            addCommonBrokerUpdateStep(brokers.get(i));
        }
        // master取消注册
        addBrokerAutoUpdateStep(master, UNREGISTER);
        // master更新
        addCommonBrokerUpdateStep(master);
        // master注册
        addBrokerAutoUpdateStep(master, REGISTER);
        // master恢复写入
        addBrokerAutoUpdateStep(master, RECOVER_WRITE);
    }

    private void addBrokerAutoUpdateStep(Broker broker, Action action) {
        steps.add(BrokerAutoUpdateStep.build(steps.size(), broker, action));
    }
}
