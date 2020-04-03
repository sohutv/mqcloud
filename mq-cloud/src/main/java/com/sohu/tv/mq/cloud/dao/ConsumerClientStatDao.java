package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.ConsumerClientStat;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

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
     * 按时间和client查询
     */
    @Select("select distinct consumer from consumer_client_stat where create_date = #{date} and client like '${client}%'")
    public List<String> selectByDateAndClient(@Param("client")String client, @Param("date")Date date);

    /**
     * 删除记录
     */
    @Delete("delete from consumer_client_stat where create_date <= #{date}")
    public Integer delete(@Param("date")Date date);
}
