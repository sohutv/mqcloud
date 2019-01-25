package com.sohu.tv.mq.cloud.task.server.nmon;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class NMONFileFinderTest {

    @Autowired
    private NMONFileFinder nmonFileFinder;
    
    @Test
    public void testDownload() {
        File f = new File("d:/opt/nmon");
        nmonFileFinder.downloadAndUnzip(f);
    }

}