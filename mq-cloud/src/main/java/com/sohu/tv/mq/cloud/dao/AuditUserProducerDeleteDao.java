package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.sohu.tv.mq.cloud.bo.AuditUserProducerDelete;

/**
 * UserProducer删除审核
 * @Description: 
 * @author zhehongyuan
 * @date 2018年9月5日
 */
public interface AuditUserProducerDeleteDao {
    /**
     * 保存记录
     */
    @Insert("insert into audit_producer_delete(aid,pid,producer,topic,uid) values(#{aid},#{pid},#{producer},#{topic},#{uid})")
    public void insert(@Param("aid") long aid, @Param("pid") long pid, @Param("producer") String producer, 
            @Param("topic") String topic, @Param("uid") long uid);
    
    /**
     * 根据aid查询AuditUserProducerDelete
     */
    @Select("select * from audit_producer_delete where aid = #{aid}")
    public AuditUserProducerDelete selectByAid(@Param("aid") long aid);
    
    /**
     * 根据uid和producer查询AuditUserProducerDelete
     */
    @Select("select * from audit_producer_delete where uid = #{uid} and producer = #{producer}")
    public List<AuditUserProducerDelete> selectByUidAndProducer(@Param("uid") long uid, @Param("producer") String producer);
}
