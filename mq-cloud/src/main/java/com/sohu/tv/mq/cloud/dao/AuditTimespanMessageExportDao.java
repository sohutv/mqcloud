package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.AuditTimespanMessageConsume;
import com.sohu.tv.mq.cloud.bo.AuditTimespanMessageExport;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 时间段消息导出dao
 *
 * @author yongfeigao
 * @date 2023/9/21
 */
public interface AuditTimespanMessageExportDao {
    /**
     * 保存记录
     */
    @Insert("insert into audit_timespan_message_export(aid,topic,start,end) "
            + "values(#{a.aid},#{a.topic},#{a.start},#{a.end})")
    public void insert(@Param("a") AuditTimespanMessageExport auditTimespanMessageExport);

    /**
     * 根据aid查询
     */
    @Select("select * from audit_timespan_message_export where aid = #{aid}")
    public AuditTimespanMessageExport selectByAid(@Param("aid") long aid);
}
