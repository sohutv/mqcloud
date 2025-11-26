package com.sohu.tv.mq.cloud.web.controller.operate;

import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

public class AutoOperateTest {
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    public String addr() {
        String addr = System.getProperty("addr");
        if (addr != null) {
            return addr;
        }
        return "127.0.0.1:9876";
    }

    public String token() {
        return mqCloudConfigHelper.getAutoOperateToken();
    }

    public HttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", addr().split(":")[0]);
        return request;
    }
}
