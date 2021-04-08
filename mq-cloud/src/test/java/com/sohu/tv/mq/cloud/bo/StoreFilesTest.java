package com.sohu.tv.mq.cloud.bo;

import org.junit.Assert;
import org.junit.Test;

public class StoreFilesTest {

    @Test
    public void test() {
        String file = "/consumequeue/vrs-vcms-topic/0/00000000000000000000";
        long size = 1024;
        StoreFiles storeFiles = new StoreFiles();
        storeFiles.addStoreFile(file, size);
        
        file = "/consumequeue/vrs-vcms-topic/2/00000000000000000000";
        size = 1024;
        storeFiles.addStoreFile(file, size);
        
        file = "/consumequeue/vrs-topic/2/00000000000000000000";
        size = 1024;
        storeFiles.addStoreFile(file, size);
        
        Assert.assertEquals(2, storeFiles.getStoreEntryMap());
    }

}
