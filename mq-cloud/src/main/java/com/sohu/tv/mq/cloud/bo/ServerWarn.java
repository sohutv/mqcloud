package com.sohu.tv.mq.cloud.bo;

import java.util.List;

/**
 * 服务器警告
 * 
 * @author yongfeigao
 * @date 2021年9月22日
 */
public class ServerWarn {
    private String ip;
    private String host;
    private String ipLink;

    private List<ServerWarnItem> list;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIpLink() {
        return ipLink;
    }

    public void setIpLink(String ipLink) {
        this.ipLink = ipLink;
    }

    public List<ServerWarnItem> getList() {
        return list;
    }

    public void setList(List<ServerWarnItem> list) {
        this.list = list;
    }

    public static class ServerWarnItem {
        private String type;
        private String value;
        private String threshold;

        public ServerWarnItem(String type, String value, String threshold) {
            this.type = type;
            this.value = value;
            this.threshold = threshold;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getThreshold() {
            return threshold;
        }

        public void setThreshold(String threshold) {
            this.threshold = threshold;
        }
    }
}
