package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.ConsumerClientMetrics;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 消费者客户端统计dao
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/27
 */
public interface ConsumerClientMetricsDao {

    /**
     * 插入记录
     */
    @Insert("<script>insert into consumer_client_metrics(consumer,client,max,avg,count,stat_time,create_date,create_time"
            + "<if test=\"s.exception != null\">,exception</if>"
            + ") values(#{s.consumer},#{s.client},#{s.max},#{s.avg},#{s.count},#{s.statTime},#{s.createDate},#{s.createTime}"
            + "<if test=\"s.exception != null\">,#{s.exception}</if>"
            + ")</script>")
    public void insert(@Param("s") ConsumerClientMetrics consumerClientMetrics);

    /**
     * 根据日期查询记录
     */
    @Select("select * from consumer_client_metrics where create_date = #{createDate} and consumer = #{consumer}")
    public List<ConsumerClientMetrics> selectByDate(@Param("consumer")String consumer, @Param("createDate")int createDate);

    /**
     * 根据上报时间查询记录
     */
    @Select("select * from consumer_client_metrics where consumer = #{consumer} and stat_time = #{statTime}")
    public List<ConsumerClientMetrics> selectByStatTime(@Param("consumer") String consumer, @Param("statTime") int statTime);


    /**
     * 删除
     * @return
     */
    @Delete("delete from consumer_client_metrics where create_date=#{createDate}")
    public Integer delete(@Param("createDate")int createDate);

    /**
     * 获取多个consumer客户端指标
     */
    @Select("<script>select consumer, sum(count) count, create_date, create_time from consumer_client_metrics "
            + "where create_date = #{createDate} and consumer in "
            + "<foreach collection=\"consumers\" item=\"consumer\" separator=\",\" open=\"(\" close=\")\">#{consumer}</foreach> "
            + "group by consumer, create_time</script>")
    public List<ConsumerClientMetrics> selectListByDate(@Param("consumers") Collection<String> consumers, @Param("createDate")int createDate);
}
