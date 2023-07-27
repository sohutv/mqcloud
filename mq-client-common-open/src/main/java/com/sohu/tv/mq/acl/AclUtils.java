/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sohu.tv.mq.acl;

import org.apache.rocketmq.remoting.protocol.RemotingCommand;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.SortedMap;

public class AclUtils {
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    public static byte[] combineRequestContent(RemotingCommand request, SortedMap<String, String> fieldsMap) {
        try {
            StringBuilder sb = new StringBuilder("");
            for (Map.Entry<String, String> entry : fieldsMap.entrySet()) {
                if (!SessionCredentials.SIGNATURE.equals(entry.getKey())) {
                    sb.append(entry.getValue());
                }
            }

            return AclUtils.combineBytes(sb.toString().getBytes(CHARSET), request.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Incompatible exception.", e);
        }
    }

    public static byte[] combineBytes(byte[] b1, byte[] b2) {
        if (b1 == null || b1.length == 0) return b2;
        if (b2 == null || b2.length == 0) return b1;
        byte[] total = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, total, 0, b1.length);
        System.arraycopy(b2, 0, total, b1.length, b2.length);
        return total;
    }

    public static String calSignature(byte[] data, String secretKey) {
        String signature = AclSigner.calSignature(data, secretKey);
        return signature;
    }
}
