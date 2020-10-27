package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditConsumer;
/**
 * 消费者审核
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月24日
 */
public interface AuditConsumerDao {
    /**
     * 保存记录
     * @param auditConsumer
     * @return
     */
    @Insert("insert into audit_consumer(aid,tid,consumer,consume_way,trace_enabled,permits_per_second) "
            + "values(#{ac.aid},#{ac.tid},#{ac.consumer},#{ac.consumeWay},#{ac.traceEnabled},#{ac.permitsPerSecond})")
    public Integer insert(@Param("ac") AuditConsumer auditConsumer);

    /**
     * 查询记录
     * @param aid
     * @return
     */
    @Select("select * from audit_consumer where aid=#{aid}")
    public AuditConsumer selectByAid(@Param("aid") long aid);
}
