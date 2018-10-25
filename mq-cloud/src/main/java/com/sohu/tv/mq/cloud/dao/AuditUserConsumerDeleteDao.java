package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditUserConsumerDelete;

/**
 * UserConsumer删除审核
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月7日
 */
public interface AuditUserConsumerDeleteDao {
    /**
     * 保存记录
     */
    @Insert("insert into audit_user_consumer_delete(aid,ucid,consumer,topic,uid) values(#{aid},#{ucid},#{consumer},#{topic},#{uid})")
    public void insert(@Param("aid") long aid, @Param("ucid") long ucid, @Param("consumer") String consumer,
            @Param("topic") String topic, @Param("uid") long uid);

    /**
     * 根据aid查询AuditUserProducerDelete
     */
    @Select("select * from audit_user_consumer_delete where aid = #{aid}")
    public AuditUserConsumerDelete selectByAid(@Param("aid") long aid);
    
    /**
     * 根据uid和consumer查询AuditUserProducerDelete
     */
    @Select("select * from audit_user_consumer_delete where uid = #{uid} and consumer = #{consumer}")
    public List<AuditUserConsumerDelete> selectByUidAndConsumer(@Param("uid") long uid, @Param("consumer") String consumer);
}
