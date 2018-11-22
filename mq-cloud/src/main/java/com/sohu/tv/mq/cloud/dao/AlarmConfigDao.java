package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;

/**
 * 预警配置项
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月26日
 */
public interface AlarmConfigDao {

    /**
     * 查询所有配置记录
     */
    @Select("select * from warn_config")
    public List<AlarmConfig> selectAll();

    /**
     * 查询记录
     */
    @Select("select * from warn_config where consumer=#{consumer}")
    public AlarmConfig selectByConsumer(@Param("consumer") String consumer);

    /**
     * 删除记录
     */
    @Delete("delete from warn_config where consumer=#{consumer}")
    public Integer deleteByConsumer(@Param("consumer") String consumer);

    /**
     * 插入记录
     */
    @Insert("replace into warn_config(consumer,accumulate_time,accumulate_count,block_time,consumer_fail_count,"
            + "warn_unit_time,warn_unit_count,ignore_warn)"
            + "values(#{alarmConfig.consumer},#{alarmConfig.accumulateTime},#{alarmConfig.accumulateCount},"
            + "#{alarmConfig.blockTime},#{alarmConfig.consumerFailCount},"
            + "#{alarmConfig.warnUnitTime},#{alarmConfig.warnUnitCount},#{alarmConfig.ignoreWarn})")
    public Integer insert(@Param("alarmConfig") AlarmConfig alarmConfig);
}
