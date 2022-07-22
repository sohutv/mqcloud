package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.serializable.MessageSerializerEnum;
import com.sohu.tv.mq.util.CommonUtil;
import com.sohu.tv.mq.util.JSONUtil;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

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
    private String realTopic;
    private String consumer;
    // broker端的msgid
    private String offsetMsgId;
    
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
    
    public String getRealTopic() {
        return realTopic;
    }

    public void setRealTopic(String realTopic) {
        this.realTopic = realTopic;
    }

    public String getConsumer() {
        return consumer;
    }
    
    public String getRealConsumer() {
        if (CommonUtil.isRetryTopic(consumer)) {
            return consumer.substring(MixAll.RETRY_GROUP_TOPIC_PREFIX.length());
        } else if (CommonUtil.isDeadTopic(consumer)) {
            return consumer.substring(MixAll.DLQ_GROUP_TOPIC_PREFIX.length());
        }
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    /**
     * 将某些属性转换为json
     * @return
     */
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("qid", getQueueId());
        map.put("offset", getQueueOffset());
        map.put("broker", address(getStoreHost()));
        map.put("born", getBornTimestamp());
        map.put("client", address(getBornHost()));
        map.put("store", getStoreTimestamp());
        return JSONUtil.toJSONString(map);
    }
    
    private String address(SocketAddress addr) {
        StringBuilder sb = new StringBuilder();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) addr;
        sb.append(inetSocketAddress.getAddress().getHostAddress());
        sb.append(":");
        sb.append(inetSocketAddress.getPort());
        return sb.toString();
    }
    
//    /**
//     * 获取offsetMsgId
//     * @return
//     */
//    public String getOffsetMsgId() {
//        int msgIdLength = (getSysFlag() & MessageSysFlag.STOREHOSTADDRESS_V6_FLAG) == 0 ? 4 + 4 + 8 : 16 + 4 + 8;
//        ByteBuffer byteBufferMsgId = ByteBuffer.allocate(msgIdLength);
//        return MessageDecoder.createMessageId(byteBufferMsgId, getStoreHostBytes(), getCommitLogOffset());
//    }

    @Override
    public String toString() {
        return super.toString();
    }
    
    public String getOffsetMsgId() {
        return offsetMsgId;
    }

    public void setOffsetMsgId(String offsetMsgId) {
        this.offsetMsgId = offsetMsgId;
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