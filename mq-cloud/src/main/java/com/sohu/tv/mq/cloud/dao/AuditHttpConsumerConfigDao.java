package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.AuditHttpConsumerConfig;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * http消费者配置审核
 *
 * @author yongfeigao
 * @date 2024年7月12日
 */
public interface AuditHttpConsumerConfigDao {
    /**
     * 插入记录
     */
    @Insert("<script>insert into audit_http_consumer_config(aid, consumer_id"
            + "<if test=\"cfg.pullSize != null\">,pull_size</if>"
            + "<if test=\"cfg.pullTimeout != null\">,pull_timeout</if>"
            + "<if test=\"cfg.consumeTimeout != null\">,consume_timeout</if>"
            + ") values(#{cfg.aid}, #{cfg.consumerId}"
            + "<if test=\"cfg.pullSize != null\">,#{cfg.pullSize}</if>"
            + "<if test=\"cfg.pullTimeout != null\">,#{cfg.pullTimeout}</if>"
            + "<if test=\"cfg.consumeTimeout != null\">,#{cfg.consumeTimeout}</if>"
            + ")</script>")
    public Integer insert(@Param("cfg") AuditHttpConsumerConfig auditHttpConsumerConfig);

    /**
     * 查询记录
     */
    @Select("select * from audit_http_consumer_config where aid = #{aid}")
    public AuditHttpConsumerConfig select(@Param("aid") long aid);

    /**
     * 查询未审核的数量
     */
    @Select("select count(1) from audit_http_consumer_config ac, audit where ac.consumer_id = #{cid} and ac.aid = audit.id and audit.status = 0")
    public Integer selectUnAuditCount(@Param("cid") long cid);
}
