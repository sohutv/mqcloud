package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.Audit;

public interface AuditDao {

    @Options(useGeneratedKeys = true, keyProperty = "audit.id")
    @Insert("<script>insert into audit(uid,type"
            + "<if test=\"audit.info != null\">,info</if>"
            + "<if test=\"audit.status != null\">,status</if>"
            + "<if test=\"audit.auditor != null\">,auditor</if>"
            + ") values(#{audit.uid},#{audit.type}"
            + "<if test=\"audit.info != null\">,#{audit.info}</if>"
            + "<if test=\"audit.status != null\">,#{audit.status}</if>"
            + "<if test=\"audit.auditor != null\">,#{audit.auditor}</if>"
            + ")</script>")
    public Long insert(@Param("audit") Audit audit);

    @Select("<script>select * from audit where 1 = 1" +
            "<if test=\"audit.type !=-1\"> and type=#{audit.type}</if>" +
            "<if test=\"audit.status !=-1\">and status=#{audit.status}</if>" +
            " order by id desc</script>")
    public List<Audit> select(@Param("audit") Audit audit);
    
    /**
     * 分页查询
     * @param audit
     * @return
     */
    @Select("<script>select count(1) from audit where 1 = 1" +
            "<if test=\"audit.type !=-1\"> and type=#{audit.type}</if>" +
            "<if test=\"audit.status !=-1\">and status=#{audit.status}</if></script>")
    public Integer selectByPageCount(@Param("audit") Audit audit);
    
    /**
     * 分页查询
     * @param audit
     * @return
     */
    @Select("<script>select * from audit where 1 = 1" +
            "<if test=\"audit.type !=-1\"> and type=#{audit.type}</if>" +
            "<if test=\"audit.status !=-1\">and status=#{audit.status}</if>" +
            " order by id desc limit #{m},#{n}</script>")
    public List<Audit> selectByPage(@Param("audit") Audit audit, @Param("m") int offset, @Param("n") int size);

    /**
     * 按id查询
     * 
     * @param id
     * @return
     */
    @Select("select * from audit where id=#{id}")
    public Audit selectById(@Param("id") long id);

    /**
     * 更新
     * @param audit
     */
    @Update("<script> update audit set auditor=#{audit.auditor} " +
            "<if test=\"audit.status != -1\">,status=#{audit.status} </if>" +
            "<if test=\"audit.refuseReason !=null\">,refuse_reason=#{audit.refuseReason} </if>" +
            " where id=#{audit.id} and status = #{os} " +
            "</script>")
    public Integer update(@Param("audit") Audit audit, @Param("os") int oldStatus);
    
    /**
     * 根据uid查询审核记录
     * @param uid
     * @return
     */
    @Select("select * from audit where uid = #{uid} order by id desc limit #{m},#{n}")
    public List<Audit> selectByUid(@Param("uid") long uid, @Param("m") int offset, @Param("n") int size);
    
    /**
     * 根据uid查询审核记录量
     * @param uid
     * @return
     */
    @Select("select count(1) from audit where uid = #{uid}")
    public Integer selectCountByUid(@Param("uid") long uid);
    
    /**
     * 更新状态
     * @param audit
     */
    @Update("update audit set status=#{ns} where id=#{id} and status = #{os}")
    public Integer updateStatus(@Param("id") long id, @Param("ns") int newStatus, @Param("os") int oldStatus);
}
