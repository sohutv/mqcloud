package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
     * 查询所有用户配置记录
     */
    @Select("select * from warn_config where uid != 0")
    public List<AlarmConfig> selectUserAlarmConfig();

    /**
     * 查询记录
     */
    @Select("select * from warn_config where uid=#{uid}")
    public List<AlarmConfig> selectByUid(@Param("uid") long uid);

    /**
     * 查询记录
     */
    @Select("select * from warn_config where uid=#{uid} and topic=#{topic}")
    public AlarmConfig selectByUidAndTopic(@Param("uid") long uid, @Param("topic") String topic);
    
    /**
     * 查询记录
     */
    @Select("select * from warn_config where id=#{id}")
    public AlarmConfig selectByID(@Param("id") long id);

    /**
     * 更新记录
     * 
     * @param topic
     */
    @Update("<script>update warn_config set id=id ,uid=#{alarmConfig.uid} "
            + ",topic=#{alarmConfig.topic} ,accumulate_time=#{alarmConfig.accumulateTime}"
            + ",accumulate_count=#{alarmConfig.accumulateCount} ,block_time=#{alarmConfig.blockTime}"
            + ",consumer_fail_count=#{alarmConfig.consumerFailCount} ,ignore_topic=#{alarmConfig.ignoreTopic}"
            + ",warn_unit_time=#{alarmConfig.warnUnitTime} ,warn_unit_count=#{alarmConfig.warnUnitCount}"
            + ",ignore_warn=#{alarmConfig.ignoreWarn} "
            + " where id=#{alarmConfig.id}</script>")
    public Integer update(@Param("alarmConfig") AlarmConfig alarmConfig);

    /**
     * 删除记录
     */
    @Delete("delete from warn_config where id=#{id}")
    public Integer deleteByID(@Param("id") long id);
    
    /**
     * 插入记录
     */
    @Insert("insert into warn_config(uid,topic,accumulate_time,accumulate_count,block_time,consumer_fail_count,"
            +"ignore_topic,warn_unit_time,warn_unit_count,ignore_warn)"
            +"values(#{alarmConfig.uid},#{alarmConfig.topic},#{alarmConfig.accumulateTime},#{alarmConfig.accumulateCount},"
            +"#{alarmConfig.blockTime},#{alarmConfig.consumerFailCount},#{alarmConfig.ignoreTopic},"
            +"#{alarmConfig.warnUnitTime},#{alarmConfig.warnUnitCount},#{alarmConfig.ignoreWarn})")
    public Integer insert(@Param("alarmConfig") AlarmConfig alarmConfig);
}
