package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.Map;

import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.ViewController;

/**
 * admin视图控制
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月5日
 */
public abstract class AdminViewController extends ViewController {

    /**
     * 设置返回的模板
     * 
     * @param map
     * @param view
     */
    protected void setView(Map<String, Object> map, String view) {
        Result.setView(map, adminViewModule() + "/" + view);
        //默认设置一个空数据
        Result.setResult(map, (Object)null);
    }

    public String adminViewModule() {
        return "admin/" + viewModule();
    }
    
    @Override
    protected String view() {
        return "adminTemplate";
    }
}
