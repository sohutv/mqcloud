package com.sohu.tv.mq.cloud.web.controller.admin.operate;

import com.sohu.tv.mq.cloud.bo.Proxy;
import com.sohu.tv.mq.cloud.common.model.ClientConnectionSize;
import com.sohu.tv.mq.cloud.service.ProxyService;
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
 * proxy自动运维控制器
 *
 * @author yongfeigao
 * @date 2025年11月07日
 */
@RestController
@RequestMapping("/admin/auto/operate/proxy")
public class AutoOperateProxyController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private ProxyService proxyService;

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
        Result<Proxy> proxyResult = proxyService.query(addr);
        if (proxyResult.isNotOK()) {
            logger.warn("addr:{} register failed, not found", addr);
            return proxyResult;
        }
        Proxy proxy = proxyResult.getResult();
        if (proxy.isStatusOK()) {
            logger.info("addr:{} register skipped, already register", addr);
            return Result.getOKResult();
        }
        // 发送通知
        autoOperateHelper.sendAlarm(proxy, "注册");
        Result<?> result = proxyService.updateStatusOK(proxy.getCid(), addr);
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
        Result<Proxy> proxyResult = proxyService.query(addr);
        if (proxyResult.isNotOK()) {
            logger.warn("addr:{} unregister failed, not found", addr);
            return proxyResult;
        }
        Proxy proxy = proxyResult.getResult();
        if (!proxy.isStatusOK()) {
            logger.info("addr:{} unregister skipped, already unregister", addr);
            return Result.getOKResult();
        }
        // 发送通知
        autoOperateHelper.sendAlarm(proxy, "注销");
        Result<?> result = proxyService.updateStatusError(proxy.getCid(), addr);
        logger.info("addr:{} unregister result:{}", addr, result);
        if (result.isOK()) {
            waitClientConnectionClose(proxy);
        }
        return result;
    }

    /**
     * 等待客户端连接关闭
     */
    public void waitClientConnectionClose(Proxy proxy) {
        long start = System.currentTimeMillis();
        while (!mqCloudConfigHelper.isAutoOperateTimeout(start)) {
            Result<ClientConnectionSize> rst = proxyService.getClientConnectionSize(proxy.getCid(), proxy.getAddr());
            ClientConnectionSize clientConnectionSize = rst.getResult();
            if (rst.isOK() && clientConnectionSize.getProducerConnectionSize() == 0
                    && clientConnectionSize.getConsumerConnectionSize() == 0) {
                logger.info("addr:{} all client connection closed, use:{}ms", proxy.getAddr(),
                        System.currentTimeMillis() - start);
                return;
            }
            if (clientConnectionSize == null) {
                logger.info("addr:{} wait client connection close, clientConnectionSize is null", proxy.getAddr());
            } else {
                logger.info("addr:{} wait client connection close, producerSize:{}, consumerSize:{}", proxy.getAddr(),
                        clientConnectionSize.getProducerConnectionSize(),
                        clientConnectionSize.getConsumerConnectionSize());
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.warn("waitClientConnectionClose sleep interrupted", e);
            }
        }
        logger.warn("addr:{} wait client connection close timeout, user:{}ms", proxy.getAddr(),
                System.currentTimeMillis() - start);
    }
}
