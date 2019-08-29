package com.sohu.tv.mq.cloud.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Audit.StatusEnum;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.service.AuditService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.vo.AuditVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 用户审核接口
 * 
 * @author yongfeigao
 * @date 2019年8月26日
 */
@Controller
@RequestMapping("/audit")
public class UserAuditController extends ViewController {
    
    @Autowired
    private AuditService auditService;
    
    /**
     * 获取审核列表
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    public String list(UserInfo userInfo, Map<String, Object> map) throws Exception {
        String view = viewModule() + "/list";
        Result<List<Audit>> auditListResult = auditService.queryAuditList(userInfo.getUser().getId());
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
        return view;
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
    
    @Override
    public String viewModule() {
        return "audit";
    }
}
