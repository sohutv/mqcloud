package com.sohu.tv.mq.cloud.bo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.rocketmq.common.message.MessageExt;

import com.alibaba.fastjson.JSONObject;

/**
 * 解码的消息
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月20日
 */
public class DecodedMessage extends MessageExt {
    private static final long serialVersionUID = 6615581963568753859L;
    private String decodedBody;

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

    @Override
    public String toString() {
        return super.toString();
    }
    
}
