package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.common.model.ConsumerInfo;
import com.sohu.tv.mq.cloud.common.model.ConsumerTableInfo;
import com.sohu.tv.mq.cloud.util.DateUtil;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.remoting.protocol.LanguageCode;
import org.apache.rocketmq.remoting.protocol.body.ProducerInfo;
import org.apache.rocketmq.remoting.protocol.body.ProducerTableInfo;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import static com.sohu.tv.mq.cloud.util.DateUtil.YMD_DASH_BLANK_HMS_COLON;

/**
 * 客户端链接信息
 *
 * @author yongfeigao
 * @date 2024年10月28日
 */
public class ClientConnectionInfo {
    private String client;
    private List<ConnectionInfo> connectionInfoList;

    public ClientConnectionInfo(String client) {
        this.client = client;
    }

    /**
     * 根据ProducerTableInfo构建客户端链接信息
     */
    public static List<ClientConnectionInfo> build(ProducerTableInfo producerTableInfo) {
        if (producerTableInfo == null || CollectionUtils.isEmpty(producerTableInfo.getData())) {
            return null;
        }
        return build(producerTableInfo.getData().entrySet(), ConnectionInfo::build);
    }

    /**
     * 根据ConsumerTableInfo构建客户端链接信息
     */
    public static List<ClientConnectionInfo> build(ConsumerTableInfo consumerTableInfo) {
        if (consumerTableInfo == null || CollectionUtils.isEmpty(consumerTableInfo.getData())) {
            return null;
        }
        return build(consumerTableInfo.getData().entrySet(), ConnectionInfo::build);
    }

    private static <T> List<ClientConnectionInfo> build(Set<Entry<String, List<T>>> entrySet,
                                                        Function<T, ConnectionInfo> connectionInfoFunction) {
        List<ClientConnectionInfo> clientConnectionInfoList = new ArrayList<>();
        for (Entry<String, List<T>> entry : entrySet) {
            ClientConnectionInfo clientConnectionInfo = new ClientConnectionInfo(entry.getKey());
            for (T t : entry.getValue()) {
                clientConnectionInfo.addConnectionInfo(connectionInfoFunction.apply(t));
            }
            clientConnectionInfo.getConnectionInfoList().sort(Comparator.comparing(ConnectionInfo::getLastUpdateTimestamp));
            clientConnectionInfoList.add(clientConnectionInfo);
        }
        // 按链接量逆序
        clientConnectionInfoList.sort(Comparator.comparing(ClientConnectionInfo::getConnectionInfoListSize).reversed()
                .thenComparing(ClientConnectionInfo::getClient));
        return clientConnectionInfoList;
    }

    public String getClient() {
        return client;
    }

    public List<ConnectionInfo> getConnectionInfoList() {
        return connectionInfoList;
    }

    public int getConnectionInfoListSize() {
        return connectionInfoList == null ? 0 : connectionInfoList.size();
    }

    public void addConnectionInfo(ConnectionInfo connectionInfo) {
        if (connectionInfoList == null) {
            connectionInfoList = new ArrayList<>();
        }
        connectionInfoList.add(connectionInfo);
    }

    public static class ConnectionInfo {
        private String clientId;
        private String remoteAddr;
        private LanguageCode language;
        private int version;
        private long lastUpdateTimestamp;
        private boolean isProducer;

        public static ConnectionInfo build(ProducerInfo producerInfo) {
            ConnectionInfo connectionInfo = new ConnectionInfo();
            connectionInfo.clientId = producerInfo.getClientId();
            connectionInfo.setRemoteAddr(producerInfo.getRemoteIP());
            connectionInfo.language = producerInfo.getLanguage();
            connectionInfo.version = producerInfo.getVersion();
            connectionInfo.lastUpdateTimestamp = producerInfo.getLastUpdateTimestamp();
            connectionInfo.isProducer = true;
            return connectionInfo;
        }

        public static ConnectionInfo build(ConsumerInfo consumerInfo) {
            ConnectionInfo connectionInfo = new ConnectionInfo();
            connectionInfo.clientId = consumerInfo.getClientId();
            connectionInfo.setRemoteAddr(consumerInfo.getRemoteIP());
            connectionInfo.language = consumerInfo.getLanguage();
            connectionInfo.version = consumerInfo.getVersion();
            connectionInfo.lastUpdateTimestamp = consumerInfo.getLastUpdateTimestamp();
            return connectionInfo;
        }

        public void setRemoteAddr(String ip) {
            if (ip != null) {
                if (ip.startsWith("/")) {
                    remoteAddr = ip.substring(1);
                } else {
                    remoteAddr = ip;
                }
            }
        }

        public String getClientId() {
            return clientId;
        }

        public String getRemoteAddr() {
            return remoteAddr;
        }

        public LanguageCode getLanguage() {
            return language;
        }

        public String getVersionDesc() {
            return MQVersion.getVersionDesc(version);
        }

        public String getLastUpdateTimestampReadable() {
            return DateUtil.getFormat(YMD_DASH_BLANK_HMS_COLON).format(new Date(lastUpdateTimestamp));
        }

        public long getLastUpdateTimestamp() {
            return lastUpdateTimestamp;
        }

        public String getRole() {
            return isProducer ? "producer" : "consumer";
        }

        @Override
        public String toString() {
            return "ConnectionInfo{" +
                    "clientId='" + clientId + '\'' +
                    ", remoteAddr='" + remoteAddr + '\'' +
                    ", language=" + language +
                    ", version=" + version +
                    ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ClientConnectionInfo{" +
                "client='" + client + '\'' +
                ", connectionInfoList=" + connectionInfoList +
                '}';
    }
}
