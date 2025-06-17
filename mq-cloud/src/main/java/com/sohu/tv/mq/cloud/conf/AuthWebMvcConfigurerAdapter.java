package com.sohu.tv.mq.cloud.conf;

import com.sohu.tv.mq.cloud.processor.EmptyStringModelAttributeMethodProcessor;
import com.sohu.tv.mq.cloud.processor.UserInfoMethodArgumentResolver;
import com.sohu.tv.mq.cloud.web.interceptor.AdminInterceptor;
import com.sohu.tv.mq.cloud.web.interceptor.AuthInterceptor;
import com.sohu.tv.mq.cloud.web.interceptor.UserGuideInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Collections;
import java.util.List;

/**
 * 拦截器配置
 *
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
@Configuration
public class AuthWebMvcConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    @Qualifier("authInterceptor")
    private AuthInterceptor authInterceptor;

    @Autowired
    @Qualifier("userGuideInterceptor")
    private UserGuideInterceptor userGuideInterceptor;

    @Autowired
    @Qualifier("adminInterceptor")
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户登录拦截器
        registry.addInterceptor(authInterceptor).excludePathPatterns("/error", "/admin/**", "/user/guide/**",
                "/cluster/**", "/register/**", "/login/**", "/rocketmq/**", "/consumer/reset/*", "/consumer/config/*"
                , "/topic/httpConsumer", "/topic/httpProducer", "/assets/**", "/plugins/**", "/software/**",
                "/favicon.ico", "/check/**");
        // 用户引导拦截器
        registry.addInterceptor(userGuideInterceptor).addPathPatterns("/user/guide/**");
        // admin模块拦截器
        registry.addInterceptor(adminInterceptor).addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(uerInfoMethodArgumentResolver());
        argumentResolvers.add(emptyStringModelAttributeMethodProcessor());
    }

    @Bean
    public UserInfoMethodArgumentResolver uerInfoMethodArgumentResolver() {
        return new UserInfoMethodArgumentResolver();
    }

    @Bean
    public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager,
            List<ViewResolver> viewResolvers) {
        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        resolver.setContentNegotiationManager(manager);
        resolver.setDefaultViews(Collections.singletonList(new MappingJackson2JsonView()));
        resolver.setViewResolvers(viewResolvers);
        return resolver;
    }

    /**
     * 配置空字符串参数处理
     * @return EmptyStringModelAttributeMethodProcessor
     */
    @Bean
    public EmptyStringModelAttributeMethodProcessor emptyStringModelAttributeMethodProcessor() {
        return new EmptyStringModelAttributeMethodProcessor(true);
    }
}
