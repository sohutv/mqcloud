package com.sohu.tv.mq.cloud.conf;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mq dao配置
 * @Description: 
 * @author yongfeigao
 * @date 2018年3月1日
 */
@Configuration
@MapperScan(basePackages = {"com.sohu.tv.mq.cloud.dao"}, sqlSessionFactoryRef = "mqSqlSessionFactory")
public class DaoConfiguration {

    @Bean
    public SqlSessionFactory mqSqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        SqlSessionFactory factory = sessionFactory.getObject();
        // 开启下划线转驼峰支持
        factory.getConfiguration().setMapUnderscoreToCamelCase(true);
        return factory;
    }
}
