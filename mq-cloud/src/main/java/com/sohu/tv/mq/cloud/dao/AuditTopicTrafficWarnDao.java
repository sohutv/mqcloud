package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.AuditTopicTrafficWarn;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author yongweizhao
 * @create 2020/9/24 11:06
 */
public interface AuditTopicTrafficWarnDao {
    /**
     * 保存记录
     */
    @Insert("insert into audit_topic_traffic_warn(aid,tid,traffic_warn_enabled) values(#{aid},#{tid},#{trafficWarnEnabled})")
    public void insert(@Param("aid") long aid, @Param("tid") long tid, @Param("trafficWarnEnabled") int trafficWarnEnabled);

    /**
     * 根据aid查询AuditTopicTrafficWarn
     */
    @Select("select * from audit_topic_traffic_warn where aid = #{aid}")
    public AuditTopicTrafficWarn selectByAid(@Param("aid") long aid);
}
