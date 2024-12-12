package com.sohu.tv.mq.cloud.conf;

import com.sohu.tv.mq.cloud.common.Destroyable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;

/**
 * 销毁配置
 *
 * @author yongfeigao
 * @date 2024年11月14日
 */
@Configuration
public class DestroyConfiguration {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private List<Destroyable> list;

    @Bean
    public Destroyable abc() {
        Collections.sort(list);
        return null;
    }

    @PreDestroy
    public void destroy() {
        Collections.sort(list);
        for (Destroyable destroyable : list) {
            try {
                logger.info("destroy:{}", destroyable);
                destroyable.destroy();
            } catch (Exception e) {
                logger.info("destroy err:{}", destroyable, e);
            }
        }
    }
}
