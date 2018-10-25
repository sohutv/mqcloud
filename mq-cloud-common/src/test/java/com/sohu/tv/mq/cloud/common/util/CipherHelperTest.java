package com.sohu.tv.mq.cloud.common.util;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class CipherHelperTest {

    @Test
    public void test() throws UnsupportedEncodingException {
        CipherHelper cipherHelper = new CipherHelper("DJs32jslkdghDSDf");
        String mail = "yongfeigao@xxx.com";
        System.out.println(cipherHelper.encrypt(mail).replaceAll("\\+", "%2B"));
        mail = "admin@xxx.com";
        System.out.println(cipherHelper.encrypt(mail).replaceAll("\\+", "%2B"));
        mail = "mqcloud@xxx.com";
        System.out.println(cipherHelper.encrypt(mail).replaceAll("\\+", "%2B"));
    }

}
