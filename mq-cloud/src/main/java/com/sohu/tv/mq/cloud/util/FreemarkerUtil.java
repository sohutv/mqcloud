package com.sohu.tv.mq.cloud.util;

import java.util.Map;

import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

/**
 * Freemarker工具类
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月26日
 */
public class FreemarkerUtil {

    /**
     * 设置静态类等
     * 
     * @param name
     * @param clz
     * @param map
     * @throws TemplateModelException
     */
    public static void set(String name, Class<?> clz, Map<String, Object> map) throws TemplateModelException {
        TemplateModel model = new BeansWrapperBuilder(new Version(Configuration.getVersion().toString())).build()
                .getStaticModels()
                .get(clz.getName());
        map.put(name, model);
    }
}
