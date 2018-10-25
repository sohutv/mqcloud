package com.sohu.tv.mq.cloud.web.controller;

import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.AuditService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 用户引导
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月13日
 */
@Controller
@RequestMapping("/user/guide")
public class UserGuideController extends ViewController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private AlertService alertService;
    
    @RequestMapping
    public String index(UserInfo userInfo, Map<String, Object> map) {
        setView(map, "index");
        return view();
    }
    
    @ResponseBody
    @RequestMapping(value="/update", method = RequestMethod.POST)
    public Result<?> update(UserInfo userInfo, @Valid User userParam) {
        if(!userInfo.getLoginId().equals(userParam.getEmail())) {
            logger.warn("not equal! cookie user:{}, param:{}", userInfo.getUser().getEmail(), userParam.getEmail());
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        boolean becomeAdmin = userParam.isAdmin();
        // 这里只允许注册普通用户
        userParam.setType(User.ORDINARY);
        if(StringUtils.isBlank(userParam.getName())) {
            userParam.setName(null);
        }
        if(StringUtils.isBlank(userParam.getMobile())) {
            userParam.setMobile(null);
        }
        Result<User> rst = userService.save(userParam);
        // 申请成为admin需要走审核流程
        if(rst.isOK() && becomeAdmin) {
            Audit audit = new Audit();
            audit.setType(TypeEnum.BECOME_ADMIN.getType());
            audit.setStatus(Audit.StatusEnum.INIT.getStatus());
            audit.setUid(rst.getResult().getId());
            Result<?> result = auditService.saveAuditAdmin(audit);
            if(result.isOK()) {
                alertService.sendAuditMail(userParam, TypeEnum.BECOME_ADMIN, "");
            }
            logger.info("admin request:{}", userParam);
        }
        return Result.getWebResult(rst);
    }
    
    @ResponseBody
    @RequestMapping(value="/skip", method = RequestMethod.POST)
    public Result<User> skip(UserInfo userInfo) {
        User user = new User();
        user.setEmail(userInfo.getLoginId());
        user.setType(User.ORDINARY);
        Result<User> rst = userService.save(user);
        return rst;
    }

    @Override
    public String viewModule() {
        return "user/guide";
    }
}
