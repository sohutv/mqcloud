package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditTopicDelete;
/**
 * topic审核删除
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
public interface AuditTopicDeleteDao {
    /**
     * 保存记录
     */
    @Insert("insert into audit_topic_delete(aid,tid,topic) values(#{aid},#{tid},#{topic})")
    public void insert(@Param("aid") long aid, @Param("tid") long tid, @Param("topic") String topic);

    /**
     * 根据aid查询AuditTopicDelete
     */
    @Select("select * from audit_topic_delete where aid = #{aid}")
    public AuditTopicDelete selectByAid(@Param("aid") long aid);
}
