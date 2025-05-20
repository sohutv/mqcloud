package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.ConsumerClientStat;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 消费者-客户端统计
 * @author yongweizhao
 * @create 2019/11/6 16:04
 */
public interface ConsumerClientStatDao {
    /**
     * 保存统计数据
     */
    @Insert("insert into consumer_client_stat(consumer,client,create_date) values(#{ccs.consumer}, #{ccs.client}, #{ccs.createDate})")
    public Integer saveConsumerClientStat(@Param("ccs")ConsumerClientStat consumerClientStat);

    /**
     * 统计数量
     */
    @Select("select count(1) from consumer_client_stat where consumer = #{ccs.consumer} and client = #{ccs.client} and create_date = #{ccs.createDate,jdbcType=DATE}")
    public Integer count(@Param("ccs")ConsumerClientStat consumerClientStat);

    /**
     * 按时间和client查询
     */
    @Select("select distinct consumer from consumer_client_stat where create_date = #{date} and client like '${client}%'")
    public List<String> selectByDateAndClient(@Param("client")String client, @Param("date")Date date);

    /**
     * 按时间和client查询
     */
    @Select("<script>select * from consumer_client_stat where create_date = #{date} and client in "
            + "<foreach collection=\"clients\" item=\"client\" separator=\",\" open=\"(\" close=\")\">#{client}</foreach> "
            + "group by client</script>")
    public List<ConsumerClientStat> selectByDateAndClients(@Param("date") String date, @Param("clients") Set<String> clients);

    /**
     * 删除记录
     */
    @Delete("delete from consumer_client_stat where create_date <= #{date}")
    public Integer delete(@Param("date")Date date);

    /**
     * 查询最新的记录
     */
    @Select("select * from consumer_client_stat where consumer = #{consumer} order by create_date desc limit 1")
    ConsumerClientStat selectLatestByConsumer(@Param("consumer") String consumer);
}
