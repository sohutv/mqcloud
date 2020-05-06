package com.sohu.tv.mq.serializable;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class StringSerializerTest {

    private StringSerializer stringSerializer = new StringSerializer();

    @Test
    public void testSerialize() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        byte[] bs = stringSerializer.serialize(map);
        Assert.assertNotNull(bs);
    }

    @Test
    public void testDeserialize() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        byte[] bs = stringSerializer.serialize(map);
        Assert.assertNotNull(bs);
        Object obj = stringSerializer.deserialize(bs);
        System.out.println(obj);
        Assert.assertNotNull(obj);
    }

}
