package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.UserMessage;

/**
 * 用户消息
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
public interface UserMessageDao {
    /**
     * 查询未读消息
     * @return
     */
    @Select("select count(1) from user_message where uid = #{uid} and status = 0")
    public Integer selectUnreadCount(@Param("uid")long uid);
    
    /**
     * 查询消息数量
     * @return
     */
    @Select("select count(1) from user_message where uid = #{uid}")
    public Integer selectCount(@Param("uid") long uid);
    
    /**
     * 查询所有消息
     * @return
     */
    @Select("select * from user_message where uid = #{uid} order by id desc limit #{m},#{n}")
    public List<UserMessage> select(@Param("uid")long uid, @Param("m") int m, @Param("n") int size);
    
    /**
     * 插入
     */
    @Insert("insert into user_message(uid,message,status,create_date) values("
            + "#{userMessage.uid},#{userMessage.message},#{userMessage.status},now())")
    public void insert(@Param("userMessage")UserMessage userMessage);
    
    /**
     * 更新
     * @return
     */
    @Update("update user_message set status=1 where id=#{id} and uid=#{uid}")
    public Integer read(@Param("id")long id, @Param("uid")long uid);
    
    /**
     * 更新
     * @return
     */
    @Update("update user_message set status=1 where uid=#{uid}")
    public Integer readByUid(@Param("uid")long uid);
}
