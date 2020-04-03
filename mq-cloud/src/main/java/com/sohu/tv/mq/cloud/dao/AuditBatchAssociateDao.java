package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditBatchAssociate;
/**
 * 批量关联dao
 * 
 * @author yongfeigao
 * @date 2020年3月18日
 */
public interface AuditBatchAssociateDao {
    /**
     * 保存记录
     * 
     * @return
     */
    @Insert("<script>insert into audit_batch_associate(aid, uids"
            + "<if test=\"c.producerIds != null\">,producer_ids</if>"
            + "<if test=\"c.consumerIds != null\">,consumer_ids</if>"
            + ") values(#{c.aid}, #{c.uids}"
            + "<if test=\"c.producerIds != null\">,#{c.producerIds}</if>"
            + "<if test=\"c.consumerIds != null\">,#{c.consumerIds}</if>"
            + ")</script>")
    public void insert(@Param("c") AuditBatchAssociate auditBatchAssociate);

    /**
     * 查询记录
     */
    @Select("select * from audit_batch_associate where aid = #{aid}")
    public AuditBatchAssociate select(@Param("aid") long aid);
}