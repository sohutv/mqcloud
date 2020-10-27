package com.sohu.tv.mq.rocketmq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.tv.mq.serializable.DefaultMessageSerializer;
import com.sohu.tv.mq.serializable.StringSerializer;

public class MessageConsumerTest {
    DefaultMessageSerializer<Object> defaultMessageSerializer = new DefaultMessageSerializer<>();
    StringSerializer<Object> stringSerializer = new StringSerializer<>();
    RocketMQConsumer personConsumer = new RocketMQConsumer("personConsumer", "testTopic");
    RocketMQConsumer stringConsumer = new RocketMQConsumer("stringConsumer", "testTopic");
    RocketMQConsumer mapConsumer = new RocketMQConsumer("mapConsumer", "testTopic");
    RocketMQConsumer errorConsumer = new RocketMQConsumer("errorConsumer", "testTopic");

    @Before
    public void init() {
        personConsumer.setConsumerCallback(new PersonConsumerCallback());
        personConsumer.setMessageSerializer(defaultMessageSerializer);
        personConsumer.initConsumerParameterTypeClass();
        
        stringConsumer.setConsumerCallback(new StringConsumerCallback());
        stringConsumer.setMessageSerializer(stringSerializer);
        stringConsumer.initConsumerParameterTypeClass();
        
        mapConsumer.setConsumerCallback(new MapConsumerCallback());
        mapConsumer.setMessageSerializer(defaultMessageSerializer);
        mapConsumer.initConsumerParameterTypeClass();
        
        errorConsumer.setConsumerCallback(new BConsumerCallback());
        errorConsumer.setMessageSerializer(defaultMessageSerializer);
        errorConsumer.initConsumerParameterTypeClass();
    }

    @Test
    public void testProtostuffSerializer() throws Exception {
        MessageConsumer messageConsumer = new MessageConsumer(personConsumer);
        messageConsumer.consumeMessage(getProtostuffMessageList(), (ConsumeConcurrentlyContext) null);
        messageConsumer.consumeMessage(getStringMessageList(), (ConsumeConcurrentlyContext) null);
    }
    
    @Test
    public void testStringSerializer() throws Exception {
        MessageConsumer messageConsumer = new MessageConsumer(stringConsumer);
        messageConsumer.consumeMessage(getProtostuffMessageList(), (ConsumeConcurrentlyContext) null);
        messageConsumer.consumeMessage(getStringMessageList(), (ConsumeConcurrentlyContext) null);
    }
    
    @Test
    public void testMapSerializer() throws Exception {
        MessageConsumer messageConsumer = new MessageConsumer(mapConsumer);
        messageConsumer.consumeMessage(getProtostuffMapMessageList(), (ConsumeConcurrentlyContext) null);
        messageConsumer.consumeMessage(getStringMapMessageList(), (ConsumeConcurrentlyContext) null);
    }
    
    @Test
    public void testErrorSerializer() throws Exception {
        MessageConsumer messageConsumer = new MessageConsumer(errorConsumer);
        messageConsumer.consumeMessage(getProtostuffMapMessageList(), (ConsumeConcurrentlyContext) null);
        messageConsumer.consumeMessage(getStringMapMessageList(), (ConsumeConcurrentlyContext) null);
    }

    private List<MessageExt> getProtostuffMessageList() {
        List<MessageExt> msgs = new ArrayList<>();
        MessageExt messageExt = new MessageExt();
        try {
            messageExt.setBody(defaultMessageSerializer.serialize(new Person("proto", 1)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        msgs.add(messageExt);
        return msgs;
    }

    private List<MessageExt> getStringMessageList() {
        List<MessageExt> msgs = new ArrayList<>();
        MessageExt messageExt = new MessageExt();
        try {
            messageExt.setBody(stringSerializer.serialize(new Person("str", 2)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        msgs.add(messageExt);
        return msgs;
    }
    
    private List<MessageExt> getProtostuffMapMessageList() {
        List<MessageExt> msgs = new ArrayList<>();
        MessageExt messageExt = new MessageExt();
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "haha");
            messageExt.setBody(defaultMessageSerializer.serialize(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
        msgs.add(messageExt);
        return msgs;
    }
    
    private List<MessageExt> getStringMapMessageList() {
        List<MessageExt> msgs = new ArrayList<>();
        MessageExt messageExt = new MessageExt();
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "haha");
            messageExt.setBody(stringSerializer.serialize(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
        msgs.add(messageExt);
        return msgs;
    }

    class PersonConsumerCallback implements ConsumerCallback<Person, MessageExt> {
        public void call(Person t, MessageExt k) throws Exception {
            System.out.println(t);
        }
    }
    
    class StringConsumerCallback implements ConsumerCallback<String, MessageExt> {
        public void call(String t, MessageExt k) throws Exception {
            System.out.println(t);
        }
    }
    
    class MapConsumerCallback implements ConsumerCallback<Map<String, Object>, MessageExt> {
        public void call(Map<String, Object> t, MessageExt k) throws Exception {
            System.out.println(t);
        }
    }
    
    abstract class AConsumerCallback<T> implements ConsumerCallback<T, MessageExt> {

        @Override
        public void call(T t, MessageExt k) throws Exception {
            consume(t);
        }
        
        public abstract void consume(T t);
    }
    class BConsumerCallback extends AConsumerCallback<String> {
        public void consume(String t) {
            System.out.println(t);
        }
    }
    

    public static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String toString() {
            return "Person [name=" + name + ", age=" + age + "]";
        }
    }
}
