package com.sohu.tv.mq.cloud.conf;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;
import java.lang.reflect.Field;

/**
 * @project mqcloud-ui
 * @description
 * @author fengwang219475
 * @date 2024/1/9 09:31:11
 * @version 1.0
 */
@Configuration
@ConditionalOnClass(FreeMarkerConfigurationFactory.class)
public class FreeMarkResourceLoaderConfig {
    
    @Bean
    public RestFreeMarkerDefaultClassLoader restFreeMarkerDefaultClassLoader(@Autowired(required = false) 
                                                                                 FreeMarkerConfigurationFactory freeMarkerConfigurationFactory){
        return new RestFreeMarkerDefaultClassLoader(freeMarkerConfigurationFactory);
    }
    
    static class RestFreeMarkerDefaultClassLoader implements InitializingBean {
        private FreeMarkerConfigurationFactory freeMarkerConfigurationFactory;

        public RestFreeMarkerDefaultClassLoader() {
        }

        public RestFreeMarkerDefaultClassLoader(FreeMarkerConfigurationFactory freeMarkerConfigurationFactory) {
            this.freeMarkerConfigurationFactory = freeMarkerConfigurationFactory;
        }

        @Override
        public void afterPropertiesSet() throws Exception{
            if (freeMarkerConfigurationFactory != null) {
                Field resourceLoader = FreeMarkerConfigurationFactory.class.getDeclaredField("resourceLoader");
                resourceLoader.setAccessible(true);
                DefaultResourceLoader defaultResourceLoader = (DefaultResourceLoader)resourceLoader.get(freeMarkerConfigurationFactory);
                defaultResourceLoader.setClassLoader(Thread.currentThread().getContextClassLoader());
            }
        }
    }
}
