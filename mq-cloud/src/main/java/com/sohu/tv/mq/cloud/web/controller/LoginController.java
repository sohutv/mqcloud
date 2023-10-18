package com.sohu.tv.mq.cloud.web.controller;

import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.common.util.CipherHelper;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 登录相关
 * 
 * @author yongfeigao
 * @date 2018年10月11日
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private CipherHelper cipherHelper;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @RequestMapping
    public String index(Map<String, Object> map) {
        Result.setResult(map, mqCloudConfigHelper.getIsOpenRegister());
        return "login/index";
    }

    /**
     * 用户注册
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public Result<?> check(@RequestParam("email") String email,
            @RequestParam(value = "password") String password,
            HttpServletResponse response) throws Exception {
        logger.info("user:{} login", email);
        Result<User> userResult = userService.queryByEmailAndPassword(email, password);
        if (Status.NO_RESULT.getKey() == userResult.getStatus()) {
            return Result.getResult(Status.LONGIN_ERROR);
        }
        if (userResult.isNotOK()) {
            return Result.getWebResult(userResult);
        }
        // 设置到cookie中
        WebUtil.setLoginCookie(response, cipherHelper.encrypt(userResult.getResult().getEmail()));
        return Result.getOKResult();
    }
}
