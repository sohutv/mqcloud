package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.TopicTrafficWarnConfig;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author yongweizhao
 * @create 2020/9/17 17:54
 */
public interface TopicTrafficWarnConfigDao {

    /**
     * 插入/更新记录
     */
    @Insert("insert into topic_traffic_warn_config(avg_multiplier, avg_max_percentage_increase, " +
            "max_max_percentage_increase, alarm_receiver, topic) values(" +
            "#{ttwc.avgMultiplier},#{ttwc.avgMaxPercentageIncrease},#{ttwc.maxMaxPercentageIncrease}," +
            "#{ttwc.alarmReceiver},#{ttwc.topic}) " +
            "on duplicate key update avg_multiplier=values(avg_multiplier), avg_max_percentage_increase=values(avg_max_percentage_increase), max_max_percentage_increase=values(max_max_percentage_increase)," +
            " alarm_receiver=values(alarm_receiver), topic=values(topic)")
    public Integer insertAndUpdate(@Param("ttwc") TopicTrafficWarnConfig topicTrafficWarnConfig);

    /**
     * 根据topic name查询
     */
    @Select("select * from topic_traffic_warn_config where topic = #{topicName}")
    public TopicTrafficWarnConfig selectByTopicName(@Param("topicName") String topicName);

    /**
     * 查询全部
     */
    @Select("select * from topic_traffic_warn_config")
    public List<TopicTrafficWarnConfig> selectAll();

    /**
     * 删除
     */
    @Delete("delete from topic_traffic_warn_config where topic = #{topicName}")
    public Integer delete(@Param("topicName") String topicName);
}
