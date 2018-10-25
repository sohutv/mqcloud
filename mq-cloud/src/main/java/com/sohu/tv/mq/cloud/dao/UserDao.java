package com.sohu.tv.mq.cloud.dao;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.User;

/**
 * 用户dao
 * @Description: 
 * @author yongfeigao
 * @date 2018年5月28日
 */
public interface UserDao {

    /**
     * 插入用户记录
     * @param user
     */
    @Options(useGeneratedKeys = true, keyProperty = "user.id")
    @Insert("<script>insert into user(email, create_date, type"
            + "<if test=\"user.name != null\">,name</if>"
            + "<if test=\"user.mobile != null\">,mobile</if>"
            + "<if test=\"user.password != null\">,password</if>"
            + ") values("
            + "#{user.email},now(),#{user.type}"
            + "<if test=\"user.name != null\">,#{user.name}</if>"
            + "<if test=\"user.mobile != null\">,#{user.mobile}</if>"
            + "<if test=\"user.password != null\">,password(#{user.password})</if>"
            + ")</script>")
    public void insert(@Param("user") User user);
    
    /**
     * 更新用户信息
     * 
     * @param user
     */
    @Update("<script>update user set id=#{user.id} "
            + "<if test=\"user.mobile != null\">,mobile=#{user.mobile}</if>"
            + "<if test=\"user.name != null\">,name=#{user.name}</if>"
            + "<if test=\"user.type != -1\">,type=#{user.type}</if>"
            + "<if test=\"user.receiveNotice != -1\">,receive_notice=#{user.receiveNotice}</if>"
            + " where id = #{user.id}</script>")
    public Integer update(@Param("user") User user);
    
    /**
     * 邮件密码查询记录
     * 
     * @param email
     */
    @Select("select * from user where email = #{email} and password = password(#{password})")
    public User selectByEmailAndPassword(@Param("email") String email, @Param("password") String password);
    
    /**
     * 查询用户记录
     * 
     * @param email
     */
    @Select("select * from user where email = #{email}")
    public User selectByEmail(@Param("email") String email);
    
    /**
     * 查询用户记录
     * @param user
     */
    @Select("select * from user")
    public List<User> selectAll();
    
    /**
     * 查询监控者
     * @param user
     */
    @Select("select * from user where receive_notice = 1")
    public List<User> selectMonitor();
    
    /**
     * 查询管理员
     * @param user
     */
    @Select("select * from user where type = 1")
    public List<User> selectAdmin();
    
    /**
     * 删除用户
     * @param user
     */
    @Update("delete from user where id = #{id}")
    public Integer delete(@Param("id") long id);
    
    /**
     * 根据id列表批量查询用户
     * @param aidList
     * @return List<User>
     */
    @Select("<script>select * from user where id in "
            + "<foreach collection=\"idList\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + "</script>")
    public List<User> selectByIdList(@Param("idList") Collection<Long> idList);
}
