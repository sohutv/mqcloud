package com.sohu.tv.mq.cloud.conf;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DaoConfigurationTest {

    @Autowired
    private DataSource dataSource;
    
    @Test
    public void test() throws SQLException {
        Assert.assertNotNull(dataSource);
    }

}
