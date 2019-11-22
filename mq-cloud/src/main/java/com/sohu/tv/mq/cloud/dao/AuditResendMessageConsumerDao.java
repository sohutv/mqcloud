package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditResendMessageConsumer;
import com.sohu.tv.mq.cloud.bo.Consumer;

/**
 * 消息重发给消费者
 * 
 * @author yongfeigao
 * @date 2019年11月5日
 */
public interface AuditResendMessageConsumerDao {
    /**
     * 保存
     * 
     * @param auditResendMessageConsumer
     * @return
     */
    @Insert("insert into audit_resend_message_consumer(aid,consumer_id) values(#{c.aid},#{c.consumerId})")
    public Integer insert(@Param("c") AuditResendMessageConsumer auditResendMessageConsumer);

    /**
     * 根据aid查询consumer
     * 
     * @param a
     * @return Consumer
     */
    @Select("select * from consumer where id in (select consumer_id from audit_resend_message_consumer where aid = #{aid})")
    public Consumer selectByAid(@Param("aid") long aid);
}
