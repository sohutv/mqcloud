package com.sohu.tv.mq.cloud.web.controller.admin.operate;

import com.sohu.tv.mq.cloud.bo.NameServer;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ns自动运维控制器
 *
 * @author yongfeigao
 * @date 2025年11月07日
 */
@RestController
@RequestMapping("/admin/auto/operate/ns")
public class AutoOperateNSController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private NameServerService nameServerService;

    @Autowired
    private AutoOperateHelper autoOperateHelper;

    /**
     * 注册
     */
    @ResponseBody
    @RequestMapping("/register")
    public Result<?> register(String addr, String token, HttpServletRequest request) {
        if (!autoOperateHelper.hasPermission(addr, token, request)) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        Result<NameServer> nameServerResult = nameServerService.query(addr);
        if (nameServerResult.isNotOK()) {
            logger.warn("addr:{} register failed, not found", addr);
            return nameServerResult;
        }
        NameServer nameServer = nameServerResult.getResult();
        if (nameServer.isStatusOK()) {
            logger.info("addr:{} register skipped, already register", addr);
            return Result.getOKResult();
        }
        // 发送通知
        autoOperateHelper.sendAlarm(nameServer, "注册");
        Result<?> result = nameServerService.updateStatusOK(nameServer.getCid(), addr);
        logger.info("addr:{} register result:{}", addr, result);
        return result;
    }

    /**
     * 注销
     */
    @ResponseBody
    @RequestMapping("/unregister")
    public Result<?> unregister(String addr, String token, HttpServletRequest request) {
        if (!autoOperateHelper.hasPermission(addr, token, request)) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        Result<NameServer> nameServerResult = nameServerService.query(addr);
        if (nameServerResult.isNotOK()) {
            logger.warn("addr:{} unregister failed, not found", addr);
            return nameServerResult;
        }
        NameServer nameServer = nameServerResult.getResult();
        if (!nameServer.isStatusOK()) {
            logger.info("addr:{} unregister skipped, already unregister", addr);
            return Result.getOKResult();
        }
        // 发送通知
        autoOperateHelper.sendAlarm(nameServer, "注销");
        Result<?> result = nameServerService.updateStatusError(nameServer.getCid(), addr);
        logger.info("addr:{} unregister result:{}", addr, result);
        if (result.isOK()) {
            waitClientConnectionClose(nameServer);
        }
        return result;
    }

    /**
     * 等待客户端连接关闭
     */
    public void waitClientConnectionClose(NameServer ns) {
        long start = System.currentTimeMillis();
        while (!mqCloudConfigHelper.isAutoOperateTimeout(start)) {
            int count = nameServerService.clientConnectionCount(ns.getCid(), ns.getIp(), ns.getPort());
            if (count == 0) {
                logger.info("addr:{} all client connection closed, use:{}ms", ns.getAddr(),
                        System.currentTimeMillis() - start);
                return;
            }
            logger.info("addr:{} wait client connection close, remaining connection size:{}", ns.getAddr(), count);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.warn("waitClientConnectionClose sleep interrupted", e);
            }
        }
        logger.warn("addr:{} wait client connection close timeout, use:{}ms", ns.getAddr(),
                System.currentTimeMillis() - start);
    }
}
