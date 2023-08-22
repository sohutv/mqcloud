package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.AuditWheelMessageCancel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description 定时消息取消DAO
 * @date 2023/7/5 18:07:05
 */
public interface AuditWheelMessageCancelDao {

    /**
     * 保存单条记录
     */
    @Insert("insert into audit_wheel_message_cancel(aid, tid, uid, uniqueId, brokerName, deliverTime, createTime) values " +
            "(#{wheelCancel.aid}, #{wheelCancel.tid}, #{wheelCancel.uid}, #{wheelCancel.uniqueId}, #{wheelCancel.brokerName}," +
            " #{wheelCancel.deliverTime}, #{wheelCancel.createTime})")
    public void insert(@Param("wheelCancel") AuditWheelMessageCancel auditWheelMessageCancel);

    /**
     * 保存批量记录
     */
    @Insert("<script>" +
            "insert into audit_wheel_message_cancel(aid, tid, uid, uniqueId, brokerName, deliverTime, createTime) values " +
            "<foreach collection=\"cancelList\" item=\"s\" separator=\",\">" +
            "(#{s.aid}, #{s.tid}, #{s.uid}, #{s.uniqueId}, #{s.brokerName}, #{s.deliverTime}, #{s.createTime})" +
            "</foreach>" +
            "</script>")
    public void insertBatch(@Param("cancelList")List<AuditWheelMessageCancel> cancelList);

    /**
     * 根据aid查询AuditWheelMessageCancel
     */
    @Select("select * from audit_wheel_message_cancel where aid = #{aid}")
    public List<AuditWheelMessageCancel> selectByAid(@Param("aid") long aid);

    /**
     * 根据aid查询AuditWheelMessageCancel中未成功取消的申请
     */
    @Select("select * from audit_wheel_message_cancel where aid = #{aid} " +
            "and not exists (select 1 from cancel_uniqid " +
            "where uniqueId = audit_wheel_message_cancel.uniqueId and tid = audit_wheel_message_cancel.tid)")
    public List<AuditWheelMessageCancel> selectNotCancelAuditByAid(@Param("aid") long aid);

    /**
     * 根据uniqueId查询AuditWheelMessageCancel
     */
    @Select("select * from audit_wheel_message_cancel where uniqueId = #{uniqueId}")
    public AuditWheelMessageCancel selectByUniqueId(@Param("uniqueId") String uniqueId);

    /**
     * 根据tid, uniqIds, status查询AuditWheelMessageCancel
     */
    @Select("<script>" +
            "select audit_wheel_message_cancel.* " +
            "from audit_wheel_message_cancel inner join audit on audit_wheel_message_cancel.aid = audit.id " +
            "where audit_wheel_message_cancel.tid = #{tid} and audit.status = #{status} " +
            "and audit_wheel_message_cancel.uniqueId in " +
            "<foreach collection=\"uniqIds\" item=\"s\" open=\"(\" separator=\",\" close=\")\">" +
            "#{s}" +
            "</foreach>" +
            "</script>")
    List<AuditWheelMessageCancel> selectByUniqIdAndTid(@Param("tid") long tid,
                                                       @Param("uniqIds") List<String> uniqIds,
                                                       @Param("status") int status);
}
