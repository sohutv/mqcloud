package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.UserGroup;

/**
 * 用户组dao
 * 
 * @author yongfeigao
 * @date 2021年12月24日
 */
public interface UserGroupDao {
    /**
     * 插入记录
     */
    @Insert("insert into user_group(name, create_date) values(#{name},now())")
    public void insert(@Param("name") String name);

    /**
     * 更新
     */
    @Update("update user_group set name=#{userGroup.name} where id = #{userGroup.id}")
    public Integer update(@Param("userGroup") UserGroup userGroup);

    /**
     * 查询
     */
    @Select("select * from user_group where id = #{id}")
    public UserGroup select(@Param("id") long id);
    
    /**
     * 查询记录
     */
    @Select("select * from user_group")
    public List<UserGroup> selectAll();
}
