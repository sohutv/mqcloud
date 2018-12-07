package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.AuditResendMessage;
/**
 * 消息重发
 * 
 * @author yongfeigao
 * @date 2018年12月7日
 */
public interface AuditResendMessageDao {
    /**
     * 批量保存
     * 
     * @param id
     * @return
     */
    @Insert("<script>insert into audit_resend_message(aid,tid,msgId) values"
            + "<foreach collection=\"msgList\" item=\"msg\" separator=\",\">"
            + "(#{msg.aid},#{msg.tid},#{msg.msgId})"
            + "</foreach>"
            + "</script>")
    public Integer insert(@Param("msgList")List<AuditResendMessage> auditResendMessageList);
    
    /**
     * 查询
     * 
     * @param id
     * @return
     */
    @Select("select * from audit_resend_message where aid=#{aid}")
    public List<AuditResendMessage> select(@Param("aid") long aid);
    
    /**
     * 查询
     * 
     * @param id
     * @return
     */
    @Select("select * from audit_resend_message where aid=#{aid} and msgId = #{msgId}")
    public AuditResendMessage selectOne(@Param("aid") long aid, @Param("msgId") String msgId);
    
    /**
     * 更新
     * 
     * @param id
     * @return
     */
    @Update("update audit_resend_message set status=#{status}, times = times + 1, send_time = now() "
            + "where aid = #{aid} and msgId = #{msgId}")
    public Integer update(@Param("aid") long aid, @Param("msgId") String msgId, @Param("status") int status);
}
