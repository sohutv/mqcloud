package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditTimespanMessageConsume;

/**
 * 时间段消息消费dao
 * 
 * @author yongfeigao
 * @date 2021年11月24日
 */
public interface AuditTimespanMessageConsumeDao {
    /**
     * 保存记录
     */
    @Insert("insert into audit_timespan_message_consume(aid,topic,consumer,client_id,start,end) "
            + "values(#{a.aid},#{a.topic},#{a.consumer},#{a.clientId},#{a.start},#{a.end})")
    public void insert(@Param("a") AuditTimespanMessageConsume auditTimespanMessageConsume);

    /**
     * 根据aid查询
     */
    @Select("select * from audit_timespan_message_consume where aid = #{aid}")
    public AuditTimespanMessageConsume selectByAid(@Param("aid") long aid);
}
