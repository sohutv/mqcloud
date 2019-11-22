package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.MessageReset;
/**
 * 消息重置
 * 
 * @author yongfeigao
 * @date 2019年10月28日
 */
public interface MessageResetDao {
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from message_reset where consumer = #{consumer}")
    public MessageReset select(@Param("consumer") String consumer);
    
    /**
     * 插入记录
     * 
     * @param messageReset
     */
    @Insert("insert into message_reset(consumer, reset_to) values(#{messageReset.consumer},#{messageReset.resetTo})"
            + " on duplicate key update reset_to=values(reset_to)")
    public Integer insert(@Param("messageReset") MessageReset messageReset);
}
