package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.TopicWarnConfig;
import org.apache.ibatis.annotations.*;

import java.util.Collection;
import java.util.List;

/**
 * topic预警配置dao
 *
 * @author yongfeigao
 * @date 2024年09月06日
 */
public interface TopicWarnConfigDao {

    /**
     * 插入
     */
    @Insert("insert into topic_warn_config(id, tid, operand_type, operator_type, threshold, warn_interval, warn_time) " +
            "values(#{config.id}, #{config.tid}, #{config.operandType}, #{config.operatorType}," +
            "#{config.threshold},#{config.warnInterval},#{config.warnTime}) on duplicate key update " +
            "operand_type=values(operand_type), operator_type=values(operator_type), threshold=values(threshold), " +
            "warn_interval=values(warn_interval), warn_time=values(warn_time)")
    public Integer insert(@Param("config") TopicWarnConfig topicWarnConfig);

    /**
     * 更新
     */
    @Update("update topic_warn_config set enabled = #{enabled} where id = #{id}")
    public Integer updateEnabled(@Param("id") long id, @Param("enabled") int enabled);

    /**
     * 查询
     */
    @Select("select * from topic_warn_config where tid = #{tid} order by id")
    public List<TopicWarnConfig> select(@Param("tid") long tid);

    /**
     * 查询
     */
    @Select("select * from topic_warn_config where id = #{id}")
    public TopicWarnConfig selectById(@Param("id") long id);

    /**
     * 删除
     */
    @Delete("delete from topic_warn_config where id = #{id}")
    public Integer delete(@Param("id") long id);


    /**
     * 批量查询
     */
    @Select("<script>select * from topic_warn_config where enabled = 1 and operand_type in "
            + "<foreach collection=\"list\" item=\"type\" separator=\",\" open=\"(\" close=\")\">#{type}</foreach>"
            + "</script>")
    public List<TopicWarnConfig> selectByOperandType(@Param("list") Collection<Integer> operandTypeList);
}
