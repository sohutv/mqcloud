package com.sohu.tv.mq.cloud.conf;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.tomcat.jdbc.pool.DataSourceProxy;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sohu.tv.mq.cloud.util.DBConfiguration;

/**
 * mq dao配置
 * @Description: 
 * @author yongfeigao
 * @date 2018年3月1日
 */
@Configuration
@MapperScan(basePackages = {"com.sohu.tv.mq.cloud.dao"}, sqlSessionTemplateRef = "mqSqlSessionTemplate")
public class DaoConfiguration {

    @Bean("mqDBConfiguration")
    @ConfigurationProperties(prefix = "spring.datasource.mq")
    public DBConfiguration dbConfiguration() {
        return new DBConfiguration();
    }

    @Bean("mqDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.tomcat")
    public DataSource dataSource(@Qualifier("mqDBConfiguration")DBConfiguration dbConfiguration) {
        return DataSourceBuilder.create().url(dbConfiguration.getUrl())
                .username(dbConfiguration.getUsername())
                .password(dbConfiguration.getPassword()).build();
    }

    @Bean("mqSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("mqDataSource")DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        SqlSessionFactory factory = sessionFactory.getObject();
        // 开启下划线转驼峰支持
        factory.getConfiguration().setMapUnderscoreToCamelCase(true);
        return factory;
    }
    
    @Bean("mqSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("mqSqlSessionFactory")SqlSessionFactory sqlSessionFactory)
            throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
    
    @Bean
    public Object mqDataSourceMBean(@Qualifier("mqDataSource") DataSource dataSource) {
        if (dataSource instanceof DataSourceProxy) {
            try {
                return ((DataSourceProxy) dataSource).createPool().getJmxPool();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
