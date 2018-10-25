package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditConsumerDelete;
/**
 * consumer删除审核
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
public interface AuditConsumerDeleteDao {
    /**
     * 保存记录
     */
    @Insert("insert into audit_consumer_delete(aid,cid,consumer,topic) values(#{aid},#{cid},#{consumer},#{topic})")
    public void insert(@Param("aid") long aid, @Param("cid") long cid, @Param("consumer") String consumer, 
            @Param("topic") String topic);

    /**
     * 根据aid查询AuditConsumerDelete
     */
    @Select("select * from audit_consumer_delete where aid = #{aid}")
    public AuditConsumerDelete selectByAid(@Param("aid") long aid);
}
