package com.sohu.tv.mq.cloud.web.controller.admin.operate;

import com.sohu.tv.mq.cloud.bo.DeployableComponent;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动化运维助手
 *
 * @author yongfeigao
 * @date 2025年11月07日
 */
@Component
public class AutoOperateHelper {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ClusterService clusterService;

    /**
     * 权限校验
     */
    public boolean hasPermission(String addr, String token, HttpServletRequest request) {
        // 校验ip是否合法
        String ip = WebUtil.getIp(request);
        String serverIp = addr.split(":")[0];
        if (!serverIp.equals(ip)) {
            logger.warn("ip:{} url:{} ip illegal", ip, WebUtil.getUrl(request));
            return false;
        }
        // 校验token是否合法
        if (!mqCloudConfigHelper.isAutoOperateToken(token)) {
            logger.warn("ip:{} url:{} token illegal", ip, WebUtil.getUrl(request));
            return false;
        }
        return true;
    }

    /**
     * 发送报警
     */
    public void sendAlarm(DeployableComponent component, String operate) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("cluster", clusterService.getMQClusterById(component.getCid()).getName());
        paramMap.put("componentName", component.getComponentName());
        paramMap.put("addr", component.getAddr());
        paramMap.put("operate", operate);
        alertService.sendWarn(null, WarnType.AUTO_OPERATE, paramMap);
    }
}
