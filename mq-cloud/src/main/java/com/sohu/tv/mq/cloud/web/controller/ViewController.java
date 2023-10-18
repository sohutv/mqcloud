package com.sohu.tv.mq.cloud.web.controller;

import java.util.Map;

import com.sohu.tv.mq.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;

/**
 * 视图控制
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月5日
 */
public abstract class ViewController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 设置返回的模板
     *
     * @param map
     * @param view
     */
    protected void setView(Map<String, Object> map, String view) {
        setView(map, view, null);
    }

    /**
     * 设置返回的模板
     * 
     * @param map
     * @param view
     */
    protected void setView(Map<String, Object> map, String view, String pageName) {
        Result.setView(map, viewModule() + "/" + view);
        //默认设置一个空数据
        Result.setResult(map, (Object)null);
        setResult(map, "version", Version.get());
        if (pageName != null) {
            setPageName(map, pageName);
        }
    }

    /**
     * 设置结果
     * 
     * @param map
     * @param view
     */
    protected <T> void setResult(Map<String, Object> map, Result<T> result) {
        Result.setResult(map, result);
    }

    /**
     * 设置结果
     * 
     * @param map
     * @param view
     */
    protected <T> void setResult(Map<String, Object> map, Object result) {
        Result.setResult(map, result);
    }
    
    /**
     * 设置结果
     * 
     * @param map
     * @param view
     */
    protected <T> void setResult(Map<String, Object> map, String key, Object value) {
        map.put(key, value);
    }
    
    /**
     * 设置分页
     * 
     * @param map
     * @param view
     */
    protected <T> void setPagination(Map<String, Object> map, PaginationParam paginationParam) {
        Result.setResult(map, paginationParam);
    }

    protected void setPageName(Map<String, Object> map, String pageName) {
        setResult(map, "pageName", pageName);
    }

    /**
     * 视图归属的模块名
     * 
     * @return
     */
    public abstract String viewModule();
    
    /**
     * 模板页面
     * @return
     */
    protected String view() {
        return "frontTemplate";
    }
}
