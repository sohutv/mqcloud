package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditResetOffset;

public interface AuditResetOffsetDao {
    /**
     * 保存记录
     * @param auditResetoffset
     * @return
     */
    @Insert("<script>insert into audit_reset_offset(aid,tid,consumer_id"
            +"<if test=\"of.offset != null\">,offset</if>"
            +"<if test=\"of.messageKey != null\">,message_key</if>"
            + ") values(#{of.aid},#{of.tid},#{of.consumerId}"
            +"<if test=\"of.offset != null\">,#{of.offset}</if>"
            +"<if test=\"of.messageKey != null\">,#{of.messageKey}</if>"
            + ")</script>")
    public Integer insert(@Param("of") AuditResetOffset auditResetOffset);

    /**
     * 查询记录
     * @param aid
     * @return
     */
    @Select("select * from audit_reset_offset where aid=#{aid}")
    public AuditResetOffset selectByAid(@Param("aid") long aid);
}
