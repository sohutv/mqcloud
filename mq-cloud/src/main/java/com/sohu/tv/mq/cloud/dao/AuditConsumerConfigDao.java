package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditConsumerConfig;
/**
 * 消费者配置审核
 * 
 * @author yongfeigao
 * @date 2020年6月4日
 */
public interface AuditConsumerConfigDao {
    /**
     * 插入记录
     * 
     * @param consumerConfig
     */
    @Insert("<script>insert into audit_consumer_config(aid, consumer_id"
            + "<if test=\"auditConsumerConfig.permitsPerSecond != null\">,permits_per_second</if>"
            + "<if test=\"auditConsumerConfig.enableRateLimit != null\">,enable_rate_limit</if>"
            + "<if test=\"auditConsumerConfig.pause != null\">,pause</if>"
            + "<if test=\"auditConsumerConfig.pauseClientId != null\">,pause_client_id</if>"
            + "<if test=\"auditConsumerConfig.unregister != null\">,unregister</if>"
            + ") values(#{auditConsumerConfig.aid}, #{auditConsumerConfig.consumerId}"
            + "<if test=\"auditConsumerConfig.permitsPerSecond != null\">,#{auditConsumerConfig.permitsPerSecond}</if>"
            + "<if test=\"auditConsumerConfig.enableRateLimit != null\">,#{auditConsumerConfig.enableRateLimit}</if>"
            + "<if test=\"auditConsumerConfig.pause != null\">,#{auditConsumerConfig.pause}</if>"
            + "<if test=\"auditConsumerConfig.pauseClientId != null\">,#{auditConsumerConfig.pauseClientId}</if>"
            + "<if test=\"auditConsumerConfig.unregister != null\">,#{auditConsumerConfig.unregister}</if>"
            + ")</script>")
    public Integer insert(@Param("auditConsumerConfig") AuditConsumerConfig auditConsumerConfig);
    
    /**
     * 查询记录
     */
    @Select("select * from audit_consumer_config where aid = #{aid}")
    public AuditConsumerConfig select(@Param("aid") long aid);
}
