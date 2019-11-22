package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditTopicTrace;

/**
 * topic trace 审核
 * 
 * @author yongfeigao
 * @date 2019年11月18日
 */
public interface AuditTopicTraceDao {
    /**
     * 保存记录
     */
    @Insert("insert into audit_topic_trace(aid,tid,trace_enabled) values(#{aid},#{tid},#{traceEnabled})")
    public void insert(@Param("aid") long aid, @Param("tid") long tid, @Param("traceEnabled") int traceEnabled);

    /**
     * 根据aid查询AuditTopicTrace
     */
    @Select("select * from audit_topic_trace where aid = #{aid}")
    public AuditTopicTrace selectByAid(@Param("aid") long aid);
}
