package com.sohu.tv.mq.cloud.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.common.util.CipherHelper;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

/**
 * 登录相关
 * 
 * @author yongfeigao
 * @date 2018年10月11日
 */
@Controller
@RequestMapping("/login")
public class LoginController extends ViewController {

    @Autowired
    private UserService userService;

    @Autowired
    private CipherHelper cipherHelper;

    @RequestMapping
    public String index(Map<String, Object> map) {
        setView(map, "index");
        return view();
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

    @Override
    public String viewModule() {
        return "login";
    }
}
