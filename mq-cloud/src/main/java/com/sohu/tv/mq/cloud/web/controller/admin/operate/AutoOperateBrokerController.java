package com.sohu.tv.mq.cloud.web.controller.admin.operate;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.MQDeployer;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.apache.rocketmq.remoting.protocol.body.BrokerStatsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * broker自动运维控制器
 *
 * @author yongfeigao
 * @date 2025年11月07日
 */
@RestController
@RequestMapping("/admin/auto/operate/broker")
public class AutoOperateBrokerController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private AutoOperateHelper autoOperateHelper;

    @Autowired
    private MQDeployer mqDeployer;

    /**
     * 启动
     */
    @ResponseBody
    @RequestMapping("/startup")
    public Result<?> startup(String addr, String token, HttpServletRequest request) {
        if (!autoOperateHelper.hasPermission(addr, token, request)) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        Result<Broker> brokerResult = brokerService.queryBroker(addr);
        if (brokerResult.isNotOK()) {
            logger.warn("addr:{} startup failed, not found", addr);
            return brokerResult;
        }
        Broker broker = brokerResult.getResult();
        if (broker.isWritable()) {
            logger.info("addr:{} startup skipped, already startup", addr);
            return Result.getOKResult();
        }
        // 发送通知
        autoOperateHelper.sendAlarm(broker, "启动");
        // 启动broker
        for (int i = 0; i < 3; i++) {
            Result<?> result = mqDeployer.startup(broker.getIp(), broker.getBaseDir(), broker.getPort(), true);
            logger.info("addr:{} startup result:{}", addr, result);
            if (result.isOK()) {
                brokerService.updateWritable(broker.getCid(), addr, true);
                return result;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.warn("startup sleep interrupted", e);
            }
        }
        return Result.getErrorResult("start failed");
    }

    /**
     * 关闭
     */
    @ResponseBody
    @RequestMapping("/_shutdown")
    public Result<?> shutdown(String addr, String token, HttpServletRequest request) {
        if (!autoOperateHelper.hasPermission(addr, token, request)) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        Result<Broker> brokerResult = brokerService.queryBroker(addr);
        if (brokerResult.isNotOK()) {
            logger.warn("addr:{} shutdown failed, not found", addr);
            return brokerResult;
        }
        Broker broker = brokerResult.getResult();
        if (!broker.isWritable()) {
            logger.info("addr:{} shutdown skipped, already shutdown", addr);
            return Result.getOKResult();
        }
        // 发送通知
        autoOperateHelper.sendAlarm(broker, "关闭");
        if (broker.isMaster()) {
            brokerService.wipeWritePerm(broker.getCid(), broker.getBrokerName(), broker.getAddr());
            logger.info("addr:{} wipe write perm", addr);
            waitWriteStop(broker);
        } else {
            brokerService.updateWritable(broker.getCid(), addr, false);
        }
        Result<?> result = mqDeployer.shutdown(broker.getIp(), broker.getPort(), broker.getBaseDir());
        logger.info("addr:{} shutdown result:{}", addr, result);
        return result;
    }

    /**
     * 等待写入停止
     */
    public void waitWriteStop(Broker broker) {
        long start = System.currentTimeMillis();
        while (!mqCloudConfigHelper.isAutoOperateTimeout(start)) {
            Result<BrokerStatsData> result = brokerService.viewBrokerPutStats(broker.getCid(), broker.getAddr());
            long putStats = 0;
            if (result.isOK()) {
                putStats = result.getResult().getStatsMinute().getSum();
                if (putStats <= 0) {
                    logger.info("addr:{} write stopped, use:{}ms", broker.getAddr(), System.currentTimeMillis() - start);
                    return;
                }
            }
            logger.info("addr:{} waiting write stop, putStats:{}", broker.getAddr(), putStats);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.warn("waitWriteStop sleep interrupted", e);
            }
        }
        logger.warn("addr:{} wait write stop timeout, use:{}", broker.getAddr(), System.currentTimeMillis() - start);
    }
}
