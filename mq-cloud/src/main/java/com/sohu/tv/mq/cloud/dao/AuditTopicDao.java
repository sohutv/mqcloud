package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AuditTopic;

public interface AuditTopicDao {
    /**
     * 保存记录
     * @param auditTopic
     */
    @Insert("insert into audit_topic(aid,name,queue_num,producer,ordered,qpd,qps,trace_enabled,"
            + "transaction_enabled,test_enabled,delay_enabled,serializer,protocol) values(#{auditTopic.aid},#{auditTopic.name},#{auditTopic.queueNum},"
            + "#{auditTopic.producer},#{auditTopic.ordered},#{auditTopic.qpd},#{auditTopic.qps},#{auditTopic.traceEnabled},"
            + "#{auditTopic.transactionEnabled},#{auditTopic.testEnabled},#{auditTopic.delayEnabled},#{auditTopic.serializer},#{auditTopic.protocol})")
    public void insert(@Param("auditTopic") AuditTopic auditTopic);

    /**
     * 根据aid查询AuditTopic
     * @param aidList
     * @return List<Topic>
     */
    @Select("select * from audit_topic where aid = #{aid}")
    public AuditTopic selectByAid(@Param("aid") long aid);
}
