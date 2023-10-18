package com.sohu.tv.mq.cloud.web.controller;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Audit.StatusEnum;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.MessageExport;
import com.sohu.tv.mq.cloud.service.AuditService;
import com.sohu.tv.mq.cloud.service.MessageExportService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.vo.AuditVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户审核接口
 * 
 * @author yongfeigao
 * @date 2019年8月26日
 */
@Controller("frontAuditController")
@RequestMapping("/audit")
public class AuditController extends ViewController {
    
    @Autowired
    private AuditService auditService;

    @Autowired
    private MessageExportService messageExportService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    /**
     * 获取审核列表
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    public String list(UserInfo userInfo, @Valid PaginationParam paginationParam, Map<String, Object> map) throws Exception {
        setView(map, "list", "我的工单");
        // 设置分页参数
        setPagination(map, paginationParam);
        // 获取警告数量
        long uid = userInfo.getUser().getId();
        Result<Integer> countResult = auditService.queryAuditCount(uid);
        if (!countResult.isOK()) {
            return view();
        }
        paginationParam.caculatePagination(countResult.getResult());
        Result<List<Audit>> auditListResult = auditService.queryAuditList(uid, paginationParam.getBegin(),
                paginationParam.getNumOfPage());
        // 拼装VO
        List<Audit> auditList = auditListResult.getResult();
        List<AuditVO> auditVOList = new ArrayList<AuditVO>(auditList.size());
        for (Audit auditObj : auditList) {
            AuditVO auditVo = new AuditVO();
            BeanUtils.copyProperties(auditObj, auditVo);
            auditVo.setStatusEnum(StatusEnum.getEnumByStatus(auditObj.getStatus()));
            auditVo.setTypeEnum(TypeEnum.getEnumByType(auditObj.getType()));
            auditVOList.add(auditVo);
        }
        setResult(map, auditVOList);
        return view();
    }
    
    /**
     * 申请详情
     * 
     * @param aid
     * @param map
     * @return
     */
    @RequestMapping("/detail")
    public String detail(@RequestParam("type") int type,
            @RequestParam(value = "aid") long aid, Map<String, Object> map) {
        TypeEnum typeEnum = TypeEnum.getEnumByType(type);
        Result<?> result = auditService.detail(typeEnum, aid);
        setResult(map, result);
        return viewModule() + "/" + typeEnum.getView();
    }
    
    /**
     * 取消申请
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/cancel")
    public Result<?> cancel(UserInfo userInfo, @RequestParam(value = "aid") long aid, Map<String, Object> map) {
        logger.info("user:{} cancel audit:{}", userInfo, aid);
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        Audit audit = auditResult.getResult();
        // 权限校验
        if (!userInfo.getUser().isAdmin() && userInfo.getUser().getId() != audit.getUid()) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        // 状态校验
        if (audit.getStatus() == StatusEnum.INIT.getStatus()) {
            Result<Integer> countResult = auditService.updateAuditStatus(aid, StatusEnum.CANCEL.getStatus(), StatusEnum.INIT.getStatus());
            Integer count = countResult.getResult();
            if (count != null && count == 0) {
                return Result.getResult(Status.AUDITED);
            }
            return countResult;
        }
        return Result.getResult(Status.AUDITED);
    }

    /**
     * 消息导出详情
     *
     * @param aid
     * @param map
     * @return
     */
    @RequestMapping("/messageExportDetail")
    public String messageExportDetail(@RequestParam(value = "aid") long aid, Map<String, Object> map) {
        Result<MessageExport> messageExportResult = messageExportService.getMessageExport(aid);
        MessageExport messageExport = messageExportResult.getResult();
        if (messageExport != null && messageExport.getExportedFilePath() != null) {
            String fileName = messageExport.getExportedFilePath();
            int idx = fileName.lastIndexOf("/");
            if (idx != -1) {
                fileName = fileName.substring(idx + 1);
                if (mqCloudConfigHelper.getExportedMessageDownloadUrlPrefix() != null) {
                    try {
                        fileName = URLEncoder.encode(fileName, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                    }
                    messageExport.setExportedFilePath(mqCloudConfigHelper.getExportedMessageDownloadUrlPrefix() + fileName + ".zip");
                }
            }
        }
        setResult(map, messageExportResult);
        return viewModule() + "/messageExportDetail";
    }

    @Override
    public String viewModule() {
        return "audit";
    }
}
