package com.sohu.tv.mq.cloud.web.controller;

import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

/**
 * 注册
 * 
 * @author yongfeigao
 * @date 2018年10月12日
 */
@Controller
@RequestMapping("/register")
public class RegisterController extends ViewController {

    @Autowired
    private UserService userService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @RequestMapping
    public String index(Map<String, Object> map) {
        setView(map, "index");
        setResult(map,mqCloudConfigHelper.getIsOpenRegister());
        return view();
    }

    /**
     * 用户注册
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(@RequestParam("email") String email,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "password") String password) throws Exception {
        // 验证用户是否开启注册功能
        if (mqCloudConfigHelper.getIsOpenRegister() != null && mqCloudConfigHelper.getIsOpenRegister() != 1) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        User user = new User();
        email = email.trim();
        if(StringUtils.isBlank(email)) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        user.setEmail(email);
        password = password.trim();
        if(StringUtils.isBlank(password)) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        user.setPassword(DigestUtils.md5Hex(password));
        
        if(StringUtils.isNotBlank(name)) {
            user.setName(name);
        }
        if(StringUtils.isNotBlank(mobile)) {
            user.setMobile(mobile);
        }
        user.setType(User.ORDINARY);
        logger.info("user:{} register", user);
        Result<User> result = userService.save(user);
        return Result.getWebResult(result);
    }

    @Override
    public String viewModule() {
        return "register";
    }

}
