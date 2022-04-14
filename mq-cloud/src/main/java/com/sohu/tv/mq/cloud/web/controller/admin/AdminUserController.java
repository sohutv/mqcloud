package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserGroup;
import com.sohu.tv.mq.cloud.service.UserGroupService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.UserParam;
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
    
    @Autowired
    private UserGroupService userGroupService;
    
    /**
     * 用户列表
     * @param map
     * @return
     */
    @RequestMapping("/list")
    public String list(@RequestParam("gid") long gid, Map<String, Object> map) {
        setView(map, "list");
        Result<List<User>> userListResult = userService.queryByGroup(gid);
        Result<List<UserGroup>> userGroupListResult = userGroupService.queryAll();
        if (userGroupListResult.isNotEmpty()) {
            setResult(map, "userGroupList", userGroupListResult.getResult());
            if (userListResult.isNotEmpty()) {
                setUserGroup(userListResult.getResult(), userGroupListResult.getResult());
            }
        }
        setResult(map, userListResult);
        return view();
    }
    
    /**
     * 设置用户分组
     * 
     * @param userList
     * @param gid
     */
    private void setUserGroup(List<User> userList, List<UserGroup> userGroupList) {
        for (User user : userList) {
            for (UserGroup userGroup : userGroupList) {
                if (user.getGid() == userGroup.getId()) {
                    user.setGroupName(userGroup.getName());
                    break;
                }
            }
        }
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
    
    /**
     * 添加人员
     * 
     * @param userParam
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(@Valid UserParam userParam) {
        User user = new User();
        BeanUtils.copyProperties(userParam, user);
        Result<User> result = userService.save(user);
        return Result.getWebResult(result);
    }
    
    /**
     * 用户密码重置
     * 
     * @param uid
     * @param password
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public Result<?> resetPassword(@RequestParam("uid") int uid, @RequestParam("password") String password) {
        if (uid < 0 || password == "") {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Result<Integer> result = userService.resetPassword(uid, password);
        return result;
    }
    
    /**
     * 更新用户所属
     */
    @ResponseBody
    @RequestMapping(value = "/update/group", method = RequestMethod.POST)
    public Result<?> addToGroup(@RequestParam("gid") long gid, @RequestParam("uid") long uid) {
        User user = new User();
        user.setId(uid);
        user.setGid(gid);
        userService.update(user);
        return Result.getWebResult(userService.update(user));
    }
    
    @Override
    public String viewModule() {
        return "user";
    }

}
