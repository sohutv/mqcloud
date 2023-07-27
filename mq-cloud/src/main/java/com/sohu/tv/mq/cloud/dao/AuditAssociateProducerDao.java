package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditAssociateProducer;

/**
 * 审核关联生产者
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月17日
 */
public interface AuditAssociateProducerDao {
    
    /**
     * 保存记录
     * @param AuditAssociateProducer
     * @return
     */
    @Insert("insert into audit_associate_producer(aid, tid, uid, producer, protocol) values(#{p.aid}, #{p.tid}, #{p.uid}, #{p.producer}, #{p.protocol})")
    public void insert(@Param("p") AuditAssociateProducer auditAssociateProducer);
    
    /**
     * 查询记录
     * @return AuditAssociateProducer
     */
    @Select("select * from audit_associate_producer where aid = #{aid}")
    public AuditAssociateProducer select(@Param("aid") long aid);
    
    /**
     * 查询记录
     * @return AuditAssociateProducer
     */
    @Select("select * from audit_associate_producer where uid = #{uid} and producer = #{producer}")
    public List<AuditAssociateProducer> selectByProducerAndUid(@Param("uid") long uid, @Param("producer") String producer);
    
    /**
     * 查询记录
     * @return AuditAssociateProducer
     */
    @Select("select * from audit_associate_producer where tid != #{tid} and producer = #{producer}")
    public List<AuditAssociateProducer> selectByProducerAndTid(@Param("tid") long tid, @Param("producer") String producer);
}
