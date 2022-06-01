package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.AdminLanguage;
import com.sohu.tv.mq.cloud.bo.ClientLanguage;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: 客户端语言
 * @date 2022/5/6 16:49
 */
public class ClientLanguageVo {

    // 序号
    private Integer index;

    // 集群名称
    private String clusterName;

    // 关联主题
    private Topic topic;

    // 客户端语言版本
    private ClientLanguage clientLanguage;

    // 人员id
    private List<Long> uids;

    // 关联人员
    private List<User> relationUsers = new ArrayList<>();

    // 姓名拼接
    private String userNames;

    // 邮箱拼接
    private String userEmails;

    // 注册语言集合
    private List<AdminLanguage> languageList;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public ClientLanguage getClientLanguage() {
        return clientLanguage;
    }

    public void setClientLanguage(ClientLanguage clientLanguage) {
        this.clientLanguage = clientLanguage;
    }

    public List<User> getRelationUsers() {
        return relationUsers;
    }

    public void setRelationUsers(List<User> relationUsers) {
        this.relationUsers = relationUsers;
    }

    public String getUserNames() {
        return userNames;
    }

    public void setUserNames(String userNames) {
        this.userNames = userNames;
    }

    public String getUserEmails() {
        return userEmails;
    }

    public void setUserEmails(String userEmails) {
        this.userEmails = userEmails;
    }

    public List<AdminLanguage> getLanguageList() {
        return languageList;
    }

    public void setLanguageList(List<AdminLanguage> languageList) {
        this.languageList = languageList;
    }

    public List<Long> getUids() {
        return uids;
    }

    public void setUids(List<Long> uids) {
        this.uids = uids;
    }

    /**
     * @description: 拼装名称和Email
     * @param: * @param:
     * @return: java.lang.String
     * @author fengwang219475
     * @date: 2022/5/7 18:09
     */
    public void initAttributes(){

        if(!CollectionUtils.isEmpty(relationUsers)){
            this.userNames =  relationUsers.stream().map(user -> {
                String name = user.getName();
                String value = user.getEmail().substring(0, user.getEmail().indexOf("@"));
                if (StringUtils.isNotBlank(name)) {
                    value = name + "【" + value + "】";
                }
                return value;
            }).collect(Collectors.joining(","));

            this.userEmails = relationUsers.stream().map(User::getEmail).collect(Collectors.joining(";"));
        }

        clientLanguage.initShowLanguage();
    }

    /**
     * @description: 拆解Uid
     * @param: * @param:
     * @return: void
     * @author fengwang219475
     * @date: 2022/5/13 11:50
     */
    public void castUidStrToList(){
        if (clientLanguage != null){
            this.uids = Stream.of(clientLanguage.getRelationUids().split(","))
                    .map(Long::valueOf)
                    .sorted()
                    .collect(Collectors.toList());
        }
    }
}
