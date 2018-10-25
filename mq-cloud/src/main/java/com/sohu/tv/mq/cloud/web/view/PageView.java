package com.sohu.tv.mq.cloud.web.view;

/**
 * 页面视图
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月29日
 */
public interface PageView {
    /**
     * 具体实现的名字，亦即访问路径，即/table/{name}，需要唯一，建议name为模块名_业务名格式 例如ip_anti
     * 
     * @return String
     */
    String getPath();

    /**
     * 表格页面的title 用于显示
     * 
     * @return String
     */
    String getPageTitle();
    
    public static final String REQUEST = "_request";
}
