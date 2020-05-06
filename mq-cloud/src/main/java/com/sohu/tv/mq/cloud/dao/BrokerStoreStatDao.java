package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.common.model.BrokerStoreStat;

/**
 * broker存储统计
 * 
 * @author yongfeigao
 * @date 2020年4月26日
 */
public interface BrokerStoreStatDao {
    /**
     * 插入记录
     */
    @Insert("insert into broker_store_stat(cluster_id,broker_ip,percent90,percent99,"
            + "avg,max,count,stat_time,create_date,create_time) values"
            + "(#{s.clusterId},#{s.brokerIp},#{s.percent90},#{s.percent99},#{s.avg},#{s.max},#{s.count},#{s.statTime},"
            + "#{s.createDate},#{s.createTime})")
    public Integer insert(@Param("s") BrokerStoreStat brokerStoreStat);

    /**
     * 删除
     * 
     * @return
     */
    @Delete("delete from broker_store_stat where create_date=#{createDate}")
    public Integer delete(@Param("createDate") int createDate);

    /**
     * 根据日期查询broker记录
     */
    @Select("select * from broker_store_stat where create_date = #{createDate} and broker_ip = #{brokerIp}")
    public List<BrokerStoreStat> selectByDate(@Param("brokerIp") String brokerIp, @Param("createDate") int createDate);
}
