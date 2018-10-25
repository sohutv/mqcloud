package com.sohu.tv.mq.cloud.cache;

import org.junit.Assert;
import org.junit.Test;

public class LocalCacheTest {

    @Test
    public void test() throws InterruptedException {
        LocalCache<Object> localCache = new LocalCache<Object>();
        localCache.setName("user");
        localCache.setSize(1000);
        localCache.setExpireAfterAccess(120);
        localCache.init();
        String k = "a";
        String v = "b";
        localCache.put(k, v);
        Object obj = localCache.get(k);
        Assert.assertEquals(v, obj);
        Thread.sleep(100 * 1000);
        obj = localCache.get(k);
        Assert.assertEquals(v, obj);
        Thread.sleep(130 * 1000);
        obj = localCache.get(k);
        Assert.assertNull(obj);
    }

}
