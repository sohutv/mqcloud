package com.sohu.tv.mq.acl;

import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.sohu.tv.mq.acl.SessionCredentials.ACCESS_KEY;
import static com.sohu.tv.mq.acl.SessionCredentials.SECURITY_TOKEN;
import static com.sohu.tv.mq.acl.SessionCredentials.SIGNATURE;

/**
 * @Auther: yongfeigao
 * @Date: 2023/7/11
 */
public class AclClientRPCHook implements RPCHook {
    private final SessionCredentials sessionCredentials;

    public AclClientRPCHook(SessionCredentials sessionCredentials) {
        this.sessionCredentials = sessionCredentials;
    }

    @Override
    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {
        // Add AccessKey and SecurityToken into signature calculating.
        request.addExtField(ACCESS_KEY, sessionCredentials.getAccessKey());
        // The SecurityToken value is unnecessary,user can choose this one.
        if (sessionCredentials.getSecurityToken() != null) {
            request.addExtField(SECURITY_TOKEN, sessionCredentials.getSecurityToken());
        }
        byte[] total = AclUtils.combineRequestContent(request, parseRequestContent(request));
        String signature = AclUtils.calSignature(total, sessionCredentials.getSecretKey());
        request.addExtField(SIGNATURE, signature);
    }

    @Override
    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {

    }

    protected SortedMap<String, String> parseRequestContent(RemotingCommand request) {
        request.makeCustomHeaderToNet();
        Map<String, String> extFields = request.getExtFields();
        // Sort property
        return new TreeMap<>(extFields);
    }

    public SessionCredentials getSessionCredentials() {
        return sessionCredentials;
    }
}
