package com.sohu.tv.mq.cloud.processor;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import javax.servlet.ServletRequest;
import java.util.Iterator;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description 自定义参数处理器
 * @date 2023/3/21 14:36:24
 */
public class EmptyStringModelAttributeMethodProcessor extends ServletModelAttributeMethodProcessor {

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    public EmptyStringModelAttributeMethodProcessor(boolean annotationNotRequired) {
        super(annotationNotRequired);
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
        EmptyStringRequestDataBinder emptyStringRequestDataBinder = new EmptyStringRequestDataBinder(
                binder.getTarget(), binder.getObjectName());
        requestMappingHandlerAdapter.getWebBindingInitializer().initBinder(emptyStringRequestDataBinder, request);
        emptyStringRequestDataBinder.bind(request.getNativeRequest(ServletRequest.class));
    }
    /**
     * 空字符串数据绑定
     *
     * @author yongfeigao
     * @date 2020年8月17日
     */
    class EmptyStringRequestDataBinder extends ExtendedServletRequestDataBinder {
        public EmptyStringRequestDataBinder(Object target, String objectName) {
            super(target, objectName);
        }
        protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
            super.addBindValues(mpvs, request);
            Iterator<PropertyValue> iterator = mpvs.getPropertyValueList().iterator();
            while (iterator.hasNext()) {
                PropertyValue propertyValue = iterator.next();
                if (propertyValue.getValue().equals("")) {
                    iterator.remove();
                }
            }
        }
    }
}
