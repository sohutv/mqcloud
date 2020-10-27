package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

public class Audit {
    // 审核id
    private long id;
    // 用户uid
    private long uid;
    // 申请类型:0:新建topic,1:修改topic,2:删除topic,3:新建消费者,4:删除消费者,5:重置offset
    private int type;
    // 申请描述
    private String info;
    // 0:等待审批,1:审批通过,2:驳回
    private int status;
    // 驳回理由
    private String refuseReason;

    // 审计员(邮箱)
    private String auditor;
    private Date createTime;
    private Date updateTime;

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public void setType(int type) {
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRefuseReason() {
        return refuseReason;
    }

    public void setRefuseReason(String refuseReason) {
        this.refuseReason = refuseReason;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Audit [id=" + id + ", uid=" + uid + ", type=" + type + ", info=" + info + ", status=" + status
                + ", refuseReason=" + refuseReason + ", auditor=" + auditor + ", createTime=" + createTime
                + ", updateTime=" + updateTime + "]";
    }

    // 审批状态
    public enum StatusEnum {

        INIT(0, "待审"), 
        AGREE(1, "同意"), 
        REJECT(2, "驳回");

        private Integer status;
        private String name;

        StatusEnum(Integer status, String name) {
            this.status = status;
            this.name = name;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public static StatusEnum getEnumByStatus(Integer status) {
            for (StatusEnum statusEnum : StatusEnum.values()) {
                if (statusEnum.status.intValue() == status.intValue()) {
                    return statusEnum;
                }
            }
            return StatusEnum.INIT;
        }

        public static String getNameByStatus(Integer code) {
            return getEnumByStatus(code).name;
        }
    }

    // 类型
    public enum TypeEnum {
        NEW_TOPIC(0, "新建TOPIC", "addTopic"), 
        UPDATE_TOPIC(1, "修改TOPIC", "updateTopic"), 
        DELETE_TOPIC(2, "删除TOPIC", "deleteTopic"), 
        NEW_CONSUMER(3, "新建消费者", "addConsumer"), 
        DELETE_CONSUMER(4, "删除消费者", "deleteConsumer"), 
        RESET_OFFSET(5, "重置offset", "resetOffset"), 
        RESET_OFFSET_TO_MAX(6, "跳过堆积", "resetOffset"), 
        ASSOCIATE_PRODUCER(7, "关联生产者", "associateProducer"), 
        ASSOCIATE_CONSUMER(8, "关联消费者", "associateConsumer"), 
        BECOME_ADMIN(9, "成为管理员", "becomeAdmin"),
        DELETE_USERPRODUCER(10, "删除生产者", "deleteUserProducer"),
        DELETE_USERCONSUMER(11, "删除消费用户", "deleteUserConsumer"),
        RESEND_MESSAGE(12, "重发消息", "resendMessage"),
        RESET_RETRY_OFFSET(13, "跳过重试消息", "resetOffset"),
        UPDATE_TOPIC_TRACE(14, "修改Topic追踪", "updateTopicTrace"),
        BATCH_ASSOCIATE(15, "批量关联", "batchAssociate"), 
        PAUSE_CONSUME(16, "暂停消费", "pauseConsume"), 
        RESUME_CONSUME(17, "恢复消费", "pauseConsume"),
        LIMIT_CONSUME(18, "消费限速", "limitConsume"),
        UPDATE_TOPIC_TRAFFIC_WARN(19, "修改Topic流量预警", "updateTopicTrafficWarn"),
        ;

        private Integer type;
        private String name;
        
        private String view;

        TypeEnum(Integer type, String name) {
            this.type = type;
            this.name = name;
        }
        
        TypeEnum(Integer type, String name, String view) {
            this.type = type;
            this.name = name;
            this.view = view;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getView() {
            return view;
        }

        public static TypeEnum getEnumByType(Integer type) {
            for (TypeEnum typeEnum : TypeEnum.values()) {
                if (typeEnum.type.intValue() == type.intValue()) {
                    return typeEnum;
                }
            }
            return TypeEnum.NEW_TOPIC;
        }

        public static String getNameByType(Integer type) {
            return getEnumByType(type).name;
        }
    }
}
