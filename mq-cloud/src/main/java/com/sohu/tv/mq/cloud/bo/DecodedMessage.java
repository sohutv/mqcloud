package com.sohu.tv.mq.cloud.bo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;

import com.alibaba.fastjson.JSONObject;
import com.sohu.tv.mq.serializable.MessageSerializerEnum;

/**
 * 解码的消息
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月20日
 */
public class DecodedMessage extends MessageExt {
    private static final long serialVersionUID = 6615581963568753859L;
    private String decodedBody;
    private String broker;
    
    // 消息体类型
    private MessageBodyType messageBodyType;
    
    // 消息体序列化方式
    private MessageSerializerEnum messageBodySerializer;

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getDecodedBody() {
        return decodedBody;
    }

    public void setDecodedBody(String decodedBody) {
        this.decodedBody = decodedBody;
    }
    
    /**
     * 将某些属性转换为json
     * @return
     */
    public String toJson() {
        JSONObject obj = new JSONObject();
        obj.put("qid", getQueueId());
        obj.put("offset", getQueueOffset());
        obj.put("broker", address(getStoreHost()));
        obj.put("born", getBornTimestamp());
        obj.put("client", address(getBornHost()));
        obj.put("store", getStoreTimestamp());
        return obj.toJSONString();
    }
    
    private String address(SocketAddress addr) {
        StringBuilder sb = new StringBuilder();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) addr;
        sb.append(inetSocketAddress.getAddress().getHostAddress());
        sb.append(":");
        sb.append(inetSocketAddress.getPort());
        return sb.toString();
    }
    
    /**
     * 获取offsetMsgId
     * @return
     */
    public String getOffsetMsgId() {
        ByteBuffer byteBufferMsgId = ByteBuffer.allocate(MessageDecoder.MSG_ID_LENGTH);
        return MessageDecoder.createMessageId(byteBufferMsgId, getStoreHostBytes(), getCommitLogOffset());
    }

    @Override
    public String toString() {
        return super.toString();
    }
    
    public MessageBodyType getMessageBodyType() {
        return messageBodyType;
    }
    
    public String getMessageBodyTypeString() {
        if(messageBodyType == null) {
            return null;
        }
        return messageBodyType.getName();
    }

    public void setMessageBodyType(MessageBodyType messageBodyType) {
        this.messageBodyType = messageBodyType;
    }

    public MessageSerializerEnum getMessageBodySerializer() {
        return messageBodySerializer;
    }
    
    public void setMessageBodySerializer(MessageSerializerEnum messageBodySerializer) {
        this.messageBodySerializer = messageBodySerializer;
    }

    /**
     * 消息体类型
     * 
     * @author yongfeigao
     * @date 2019年9月6日
     */
    public enum MessageBodyType {
        STRING(1, "String"),
        BYTE_ARRAY(2, "byte[]"),
        Map(3, "Map"),
        OBJECT(4, "Object"),
        ;
        
        private int type;
        
        private String name;
        
        private MessageBodyType(int type, String name) {
            this.type = type;
            this.name = name;
        }

        public int getType() {
            return type;
        }
        
        public String getName() {
            return name;
        }
    }
}