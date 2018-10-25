package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditTopicUpdate;
/**
 * topic审核修改
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
public interface AuditTopicUpdateDao {
    /**
     * 保存记录
     */
    @Insert("insert into audit_topic_update(aid,tid,queue_num) values(#{atu.aid},#{atu.tid},#{atu.queueNum})")
    public void insert(@Param("atu") AuditTopicUpdate auditTopicUpdate);

    /**
     * 查询
     */
    @Select("select * from audit_topic_update where aid = #{aid}")
    public AuditTopicUpdate selectByAid(@Param("aid") long aid);
}
