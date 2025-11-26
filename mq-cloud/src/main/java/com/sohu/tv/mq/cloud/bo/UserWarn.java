package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * 用户警告
 * 
 * @author yongfeigao
 * @date 2021年9月13日
 */
public class UserWarn {
    // 警告
    private long uid;
    // 类型
    private int type;
    // 资源
    private String resource;
    // 创建时间
    private Date createTime;
    // 警告id
    private long wid;
    // 警告内容
    private String content;
    
    public UserWarn() {
    }
    
    public UserWarn(String content) {
        this.content = content;
    }

    public UserWarn(long uid, int type, String resource, long wid) {
        this.uid = uid;
        this.type = type;
        this.resource = resource;
        this.wid = wid;
    }

    /**
     * 警告类型
     * 
     * @author yongfeigao
     * @date 2021年9月13日
     */
    public enum WarnType {
        PRODUCE_EXCEPTION(1, "生产异常", "produceException.html"),
        CONSUME_FAIL(2, "消费失败", "consumeFail.html"),
        CONSUME_UNDONE(3, "消费堆积", "consumeUndone.html"),
        CONSUME_BLOCK(4, "消费阻塞", "consumeBlock.html"),
        CONSUME_OFFSET_MOVED(5, "消费偏移量错误", "consumeOffsetMoved.html"),
        CONSUME_SUBSCRIBE_ERROR(6, "消费订阅错误", "consumeSubscribeError.html"),
        DEAD_MESSAGE(7, "死消息", "deadMessage.html"),
        CONSUME_FALL_BEHIND(8, "消费落后", "consumeFallBehind.html"),
        TOPIC_SURGE_TRAFFIC(9, "流量突增", "topicTraffic.html"),
        BROKER_STORE_SLOW(10, "broker存储过慢", "brokerStoreSlow.html"),
        BROKER_ERROR(11, "Broker异常", "mqError.html"),
        NAMESERVER_ERROR(12, "NameServer异常", "mqError.html"),
        SLAVE_FALL_BEHIND(13, "slave同步落后", "slaveFallBehind.html"),
        SERVER_WARN(14, "服务器异常", "serverWarn.html"),
        CONTROLLER_ERROR(13, "Controller异常", "mqError.html"),
        PROXY_ERROR(14, "Proxy异常", "mqError.html"),
        MESSAGE_EXPORT_ERROR(15, "消息导出过慢", "messageExportFailed.html"),
        CAPACITY_REPORT(16, "容量日报", "capacityReport.html", false),
        TOPIC_WARN(17, "Topic流量预警", "topicWarn.html"),
        BROKER_AUTO_UPDATE_WARN(18, "broker自动更新", "brokerAutoUpdate.html", false),
        AUTO_OPERATE(19, "自动运维", "autoOperate.html", false),

        UNKNOWN(100, "未知", "unknown.html"),
        ;
        
        private int type;
        private String name;
        private String warnTemplate;
        private boolean needSave;
        
        private WarnType(int type, String name, String warnTemplate) {
            this(type, name, warnTemplate, true);
        }

        private WarnType(int type, String name, String warnTemplate, boolean needSave) {
            this.type = type;
            this.name = name;
            this.warnTemplate = warnTemplate;
            this.needSave = needSave;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }
        
        public String getWarnTemplate() {
            return warnTemplate;
        }

        public boolean isNeedSave() {
            return needSave;
        }

        public static WarnType getWarnType(int type) {
            for (WarnType warnType : WarnType.values()) {
                if (type == warnType.getType()) {
                    return warnType;
                }
            }
            return UNKNOWN;
        }
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getType() {
        return type;
    }
    
    public String getTypeName() {
        return WarnType.getWarnType(type).getName();
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getWid() {
        return wid;
    }

    public void setWid(long wid) {
        this.wid = wid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "UserWarn{" +
                "uid=" + uid +
                ", type=" + type +
                ", resource='" + resource + '\'' +
                ", createTime=" + createTime +
                ", wid=" + wid +
                ", content='" + content + '\'' +
                '}';
    }
}
