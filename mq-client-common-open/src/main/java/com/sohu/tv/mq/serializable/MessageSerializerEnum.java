package com.sohu.tv.mq.serializable;

/**
 * 消息序列化常量
 * 
 * @author yongfeigao
 * @date 2019年8月28日
 */
public enum MessageSerializerEnum {
    PROTOSTUF(0, "Protostuf", new DefaultMessageSerializer<Object>()),
    STRING(1, "String", new StringSerializer<Object>()),
    ;
    
    private int type;
    private String name;
    private MessageSerializer<?> messageSerializer;
    
    private MessageSerializerEnum(int type, String name, MessageSerializer<?> messageSerializer) {
        this.type = type;
        this.name = name;
        this.messageSerializer = messageSerializer;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MessageSerializer<?> getMessageSerializer() {
        return messageSerializer;
    }

    public void setMessageSerializer(MessageSerializer<?> messageSerializer) {
        this.messageSerializer = messageSerializer;
    }

    /**
     * 根据type获取序列化器
     * @param type
     * @return
     */
    public static MessageSerializerEnum getEnumByType(int type) {
        for (MessageSerializerEnum messageSerializerEnum : MessageSerializerEnum.values()) {
            if (messageSerializerEnum.type == type) {
                return messageSerializerEnum;
            }
        }
        return null;
    }

    /**
     * 根据type获取序列化器的名字
     * @param type
     * @return
     */
    public static String getNameByType(int type) {
        MessageSerializerEnum messageSerializerEnum = getEnumByType(type);
        if(messageSerializerEnum == null) {
            return null;
        }
        return messageSerializerEnum.name;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> MessageSerializer<T> getMessageSerializerByType(int type) {
        MessageSerializerEnum messageSerializerEnum = getEnumByType(type);
        if(messageSerializerEnum == null) {
            return null;
        }
        return (MessageSerializer<T>) messageSerializerEnum.messageSerializer;
    }
}
