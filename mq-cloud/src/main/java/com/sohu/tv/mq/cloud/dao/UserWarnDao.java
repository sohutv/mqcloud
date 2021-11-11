package com.sohu.tv.mq.cloud.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.UserWarn;

/**
 * 用户警告
 * 
 * @author yongfeigao
 * @date 2021年9月13日
 */
public interface UserWarnDao {

    /**
     * 保存警告信息
     * 
     * @param userWarn
     * @return
     */
    @Options(useGeneratedKeys = true, keyProperty = "userWarn.wid")
    @Insert("insert into warn_info(content) values(#{userWarn.content})")
    public Long insert(@Param("userWarn") UserWarn userWarn);

    /**
     * 批量插入记录
     * 
     * @param consumer
     */
    @Insert("<script>insert into user_warn(uid, type, resource, wid) values"
            + "<foreach collection=\"uwList\" item=\"uw\" separator=\",\">"
            + "(#{uw.uid},#{uw.type},#{uw.resource},#{uw.wid})"
            + "</foreach></script>")
    public Integer batchInsert(@Param("uwList") List<UserWarn> userWarnList);

    /**
     * 查询警告信息
     * 
     * @param uid
     * @param createDate
     * @param m
     * @param size
     * @return
     */
    @Select("select * from user_warn where uid = #{uid} order by create_time desc limit #{m},#{n}")
    public List<UserWarn> select(@Param("uid") long uid, @Param("m") int m, @Param("n") int size);
    
    /**
     * 查询警告详情
     * @param wid
     * @return
     */
    @Select("select * from warn_info where id = #{wid}")
    public UserWarn selectWarnInfo(@Param("wid") long wid);

    /**
     * 查询警告信息数量
     * 
     * @param uid
     * @param createDate
     * @param m
     * @param size
     * @return
     */
    @Select("select count(1) from user_warn where uid = #{uid}")
    public Integer selectCount(@Param("uid") long uid);

    /**
     * 警告数
     * 
     * @param uid
     * @param createData
     * @return
     */
    @Select("select date(create_time) createDate,count(1) count from user_warn where uid = #{uid} and create_time > #{time} group by date(create_time)")
    public List<UserWarnCount> warnCount(@Param("uid") long uid, @Param("time") Date time);
}