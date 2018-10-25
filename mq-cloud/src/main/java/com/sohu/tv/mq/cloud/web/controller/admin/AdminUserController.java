package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.Result;
/**
 * 用户
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月18日
 */
@Controller
@RequestMapping("/admin/user")
public class AdminUserController extends AdminViewController {

    @Autowired
    private UserService userService;
    
    /**
     * 用户列表
     * @param map
     * @return
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) {
        setView(map, "list");
        Result<List<User>> userListResult = userService.queryAll();
        setResult(map, userListResult);
        return view();
    }
    
    /**
     * 成为监控人员
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/monitor", method=RequestMethod.POST)
    public Result<?> monitor(@RequestParam("uid") int uid, @RequestParam("flag") int flag) {
        User user = new User();
        user.setId(uid);
        user.setReceiveNotice(flag);
        user.setType(-1);
        Result<Integer> result = userService.update(user);
        return Result.getWebResult(result);
    }
    
    @Override
    public String viewModule() {
        return "user";
    }

}
