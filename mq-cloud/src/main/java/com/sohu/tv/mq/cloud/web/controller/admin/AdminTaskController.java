package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.ShedLock;
import com.sohu.tv.mq.cloud.service.ShedLockService;
import com.sohu.tv.mq.cloud.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * 任务管理
 *
 * @author yongfeigao
 * @date 2023/9/21
 */
@Controller
@RequestMapping("/admin/task")
public class AdminTaskController extends AdminViewController {

    @Autowired
    private ShedLockService shedLockService;

    /**
     * 获取列表
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) throws Exception {
        setView(map, "list");
        Result<List<ShedLock>> listResult = shedLockService.queryAll();
        setResult(map, listResult);
        return view();
    }

    @Override
    public String viewModule() {
        return "task";
    }

}
