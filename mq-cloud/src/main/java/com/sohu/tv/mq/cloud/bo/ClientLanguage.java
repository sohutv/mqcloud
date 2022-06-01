package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.remoting.protocol.LanguageCode;

import java.util.Date;
import java.util.Objects;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: 客户端版本语言
 * @date 2022/4/26 17:46
 */
public class ClientLanguage {

    /**
     * 定义一些常量
     */
    public static final Integer PRODUCER_CLIENT_GROUP_TYPE = 0;

    public static final Integer CONSUMER_CLIENT_GROUP_TYPE = 1;

    public static final Integer MODIFY_AUTO = 0;

    public static final Integer MODIFYBYPERSON = 1;

    /**
     * cluster id
     */
    private Long cid;

    /**
     * topic id
     */
    private Long tid;

    /**
     * 生产者/消费者名称
     */
    private String clientGroupName;

    /**
     * 类型 0 生产者 1 消费者
     */
    private Integer clientGroupType;

    /**
     * 客户端语言 org.apache.rocketmq.remoting.protocol.LanguageCode
     * 注意：python依赖cpp,客户端核心逻辑全由cpp完成，所以cpp和python版本无法区别
     */
    private Byte language;

    /**
     * 版本
     */
    private String version;

    /**
     * 关联人员id集合，逗号分隔
     */
    private String relationUids;

    /**
     * 修改方式 0 自动 1 手动
     */
    private Integer modifyType;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 展示用
     */
    private String languageStr;

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getClientGroupName() {
        return clientGroupName;
    }

    public void setClientGroupName(String clientGroupName) {
        this.clientGroupName = clientGroupName;
    }

    public Integer getClientGroupType() {
        return clientGroupType;
    }

    public void setClientGroupType(Integer clientGroupType) {
        this.clientGroupType = clientGroupType;
    }

    public Byte getLanguage() {
        return language;
    }

    public void setLanguage(Byte language) {
        this.language = language;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRelationUids() {
        return relationUids;
    }

    public void setRelationUids(String relationUids) {
        this.relationUids = relationUids;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getLanguageStr() {
        return languageStr;
    }

    public void setLanguageStr(String languageStr) {
        this.languageStr = languageStr;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Integer getModifyType() {
        return modifyType;
    }

    public void setModifyType(Integer modifyType) {
        this.modifyType = modifyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientLanguage that = (ClientLanguage) o;
        return language == that.language && Objects.equals(clientGroupName, that.clientGroupName) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientGroupName, language, version);
    }

    public void initShowLanguage(){
        if (language != null){
            this.languageStr = LanguageCode.valueOf(language).name();
        }
    }
}
