package com.sohu.tv.mq.stats;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TimeSectionStatsTest {

    public static final int MAX_TIME = 3100;
    
    private TimeSectionStats timeSectionStats = new TimeSectionStats(MAX_TIME);
    
    @Test
    public void test() {
        List<Integer> timeSection = timeSectionStats.getTimeSection();
        for(int i = 0; i < timeSection.size(); ++i) {
            System.out.println(i+"="+timeSection.get(i));
        }
        for(int i = 0; i <= MAX_TIME; ++i) {
            test(i);
        }
    }

    private void test(int time) {
        int index = getIndex(time);
        int dest = getTime(index);
        int minus = dest - time;
        System.out.println(time+":"+dest+":"+index);
        if(time <= 10) {
            Assert.assertEquals(time, dest);
        } else if(time <= 100) {
            Assert.assertTrue(minus >= 0 && minus < 5);
        } else {
            Assert.assertTrue(minus >= 0 && minus < 50);
        }
    }
    
    private int getTime(int index) {
        return timeSectionStats.time(index);
    }
    
    private int getIndex(int time) {
        return timeSectionStats.index(time);
    }
}
