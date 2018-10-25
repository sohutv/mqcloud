package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditAssociateConsumer;

/**
 * 审核关联消费者
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月17日
 */
public interface AuditAssociateConsumerDao {
    /**
     * 保存记录
     * 
     * @return
     */
    @Insert("insert into audit_associate_consumer(aid, tid, cid, uid) values(#{c.aid}, #{c.tid}, #{c.cid}, #{c.uid})")
    public void insert(@Param("c") AuditAssociateConsumer auditAssociateConsumer);

    /**
     * 查询记录
     */
    @Select("select * from audit_associate_consumer where aid = #{aid}")
    public AuditAssociateConsumer select(@Param("aid") long aid);

    /**
     * 查询记录
     */
    @Select("select * from audit_associate_consumer where uid = #{uid} and cid = #{cid}")
    public List<AuditAssociateConsumer> selectByUidAndCid(@Param("uid") long uid, @Param("cid") long cid);
}
