package com.sohu.tv.mq.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SerializerTest {
    public static ObjectA generateTestObject() {
        List<ObjectB> list = new ArrayList<ObjectB>();
        ObjectB objectB = null;
        for(int i = 1; i <= 3; ++i) {
            objectB = new ObjectB();
            objectB.setI(i);
            objectB.setI(Integer.valueOf(String.valueOf(i)));
            objectB.setL(i);
            objectB.setL(Long.valueOf(String.valueOf(i)));
            objectB.setS((short)i);
            objectB.setS(Short.valueOf(String.valueOf(i)));
            objectB.setC('a');
            objectB.setC(Character.valueOf('a'));
            objectB.setB((byte)i);
            objectB.setB(Byte.valueOf(String.valueOf(i)));
            objectB.setDate(new Date());
            objectB.setStr("abc");
            String[] sarray = {"a", "b", "c"};
            objectB.setSarray(sarray);
            list.add(objectB);
        }
        
        ObjectA objectA = new ObjectA();
        objectA.setI(8);
        objectA.setI(Integer.valueOf(String.valueOf("8")));
        objectA.setL(8);
        objectA.setL(Long.valueOf(String.valueOf("8")));
        objectA.setS((short)8);
        objectA.setS(Short.valueOf(String.valueOf("8")));
        objectA.setC('a');
        objectA.setC(Character.valueOf('a'));
        objectA.setB((byte)8);
        objectA.setB(Byte.valueOf(String.valueOf("8")));
        objectA.setDate(new Date());
        objectA.setStr("abc");
        String[] sarray = {"a", "b", "c"};
        objectA.setSarray(sarray);
        objectA.setName("name");
        objectA.setObjectB(objectB);
        objectA.setBlist(list);
        
        return objectA;
    }
    
    public static class ObjectP{
        private String name;
        public void setName(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return "ObjectP [name=" + name + "]";
        }
    }
    
    public static class ObjectA extends ObjectP{
        private int i;
        private Integer I;
        private long l;
        private Long L;
        private short s;
        private Short S;
        private char c;
        private Character C;
        private byte b;
        private Byte B;
        private String str;
        private String[] sarray;
        private Date date; 
        private ObjectB objectB;
        private List<ObjectB> blist;
        public void setI(int i) {
            this.i = i;
        }
        public void setI(Integer i) {
            I = i;
        }
        public void setL(long l) {
            this.l = l;
        }
        public void setL(Long l) {
            L = l;
        }
        public void setS(short s) {
            this.s = s;
        }
        public void setS(Short s) {
            S = s;
        }
        public void setC(char c) {
            this.c = c;
        }
        public void setC(Character c) {
            C = c;
        }
        public void setB(byte b) {
            this.b = b;
        }
        public void setB(Byte b) {
            B = b;
        }
        public void setStr(String str) {
            this.str = str;
        }
        public void setSarray(String[] sarray) {
            this.sarray = sarray;
        }
        public void setDate(Date date) {
            this.date = date;
        }
        public void setObjectB(ObjectB objectB) {
            this.objectB = objectB;
        }
        public void setBlist(List<ObjectB> blist) {
            this.blist = blist;
        }
        @Override
        public String toString() {
            return "ObjectA [i=" + i + ", I=" + I + ", l=" + l + ", L=" + L + ", s=" + s + ", S=" + S + ", c=" + c
                    + ", C=" + C + ", b=" + b + ", B=" + B + ", str=" + str + ", sarray=" + Arrays.toString(sarray)
                    + ", date=" + date + ", objectB=" + objectB + ", blist=" + blist + ", toString()="
                    + super.toString() + "]";
        }
    }

    public static class ObjectB{
        private int i;
        private Integer I;
        private long l;
        private Long L;
        private short s;
        private Short S;
        private char c;
        private Character C;
        private byte b;
        private Byte B;
        private String str;
        private String[] sarray;
        private Date date;
        public void setI(int i) {
            this.i = i;
        }
        public void setI(Integer i) {
            I = i;
        }
        public void setL(long l) {
            this.l = l;
        }
        public void setL(Long l) {
            L = l;
        }
        public void setS(short s) {
            this.s = s;
        }
        public void setS(Short s) {
            S = s;
        }
        public void setC(char c) {
            this.c = c;
        }
        public void setC(Character c) {
            C = c;
        }
        public void setB(byte b) {
            this.b = b;
        }
        public void setB(Byte b) {
            B = b;
        }
        public void setStr(String str) {
            this.str = str;
        }
        public void setSarray(String[] sarray) {
            this.sarray = sarray;
        }
        public void setDate(Date date) {
            this.date = date;
        }
        @Override
        public String toString() {
            return "ObjectB [i=" + i + ", I=" + I + ", l=" + l + ", L=" + L + ", s=" + s + ", S=" + S + ", c=" + c
                    + ", C=" + C + ", b=" + b + ", B=" + B + ", str=" + str + ", sarray=" + Arrays.toString(sarray)
                    + ", date=" + date + "]";
        } 
    }
}
