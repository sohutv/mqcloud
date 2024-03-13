package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.ConsumerPauseConfig;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消费者暂停配置
 *
 * @author yongfeigao
 * @date 2023/12/12
 */
public interface ConsumerPauseConfigDao {
    /**
     * 插入记录
     *
     * @param c
     */
    @Insert("insert into consumer_pause_config(consumer,pause_client_id,unregister) values(#{c.consumer},#{c.pauseClientId},#{c.unregister})")
    public Integer insert(@Param("c") ConsumerPauseConfig consumerPauseConfig);

    /**
     * 删除记录
     */
    @Delete("delete from consumer_pause_config where consumer = #{consumer} and pause_client_id = #{pauseClientId}")
    public Integer delete(@Param("consumer") String consumer, @Param("pauseClientId") String pauseClientId);

    /**
     * 删除记录
     */
    @Delete("delete from consumer_pause_config where consumer = #{consumer}")
    public Integer deleteByConsumer(@Param("consumer") String consumer);

    /**
     * 查询记录
     */
    @Select("select * from consumer_pause_config where consumer = #{consumer}")
    public List<ConsumerPauseConfig> selectByConsumer(@Param("consumer") String consumer);

    /**
     * 查询记录
     */
    @Select("select * from consumer_pause_config")
    public List<ConsumerPauseConfig> selectAll();
}
