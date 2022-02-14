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
import com.sohu.tv.mq.cloud.web.controller.param.UserGroupParam;

/**
 * 用户组
 * 
 * @author yongfeigao
 * @date 2021年12月27日
 */
@Controller
@RequestMapping("/admin/user/group")
public class AdminUserGroupController extends AdminViewController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserGroupService userGroupService;

    /**
     * 用户组列表
     * 
     * @param map
     * @return
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) {
        Result<List<UserGroup>> userGroupListResult = userGroupService.queryAll();
        setResult(map, userGroupListResult);
        return adminViewModule() + "/list";
    }

    /**
     * 组员
     * 
     * @param map
     * @return
     */
    @RequestMapping("/members")
    public String members(@RequestParam("gid") long gid, Map<String, Object> map) {
        Result<List<User>> userListResult = userService.queryByGroup(gid);
        setResult(map, userListResult);
        return adminViewModule() + "/members";
    }

    /**
     * 更新
     * 
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Result<?> update(@Valid UserGroupParam userGroupParam, Map<String, Object> map) {
        Result<?> result = null;
        if (userGroupParam.getId() == 0) {
            result = userGroupService.save(userGroupParam.getName());
        } else {
            UserGroup userGroup = new UserGroup();
            BeanUtils.copyProperties(userGroupParam, userGroup);
            result = userGroupService.update(userGroup);
        }
        return Result.getWebResult(result);
    }

    @Override
    public String viewModule() {
        return "user/group";
    }
}
