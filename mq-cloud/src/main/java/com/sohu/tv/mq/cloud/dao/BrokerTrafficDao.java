package com.sohu.tv.mq.cloud.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.BrokerTraffic;

/**
 * broker流量
 * 
 * @author yongfeigao
 * @date 2018年9月28日
 */
public interface BrokerTrafficDao {
    /**
     * 插入记录
     * 
     * @param topic
     */
    @Insert("<script>insert into broker_traffic(ip, create_date, create_time, cluster_id "
            + "<if test=\"brokerTraffic.putCount != 0\">,put_count</if> "
            + "<if test=\"brokerTraffic.putSize != 0\">,put_size</if> "
            + "<if test=\"brokerTraffic.getCount != 0\">,get_count</if> "
            + "<if test=\"brokerTraffic.getSize != 0\">,get_size</if> "
            + ") values( "
            + "#{brokerTraffic.ip},now(),#{brokerTraffic.createTime},#{brokerTraffic.clusterId} "
            + "<if test=\"brokerTraffic.putCount != 0\">,#{brokerTraffic.putCount}</if> "
            + "<if test=\"brokerTraffic.putSize != 0\">,#{brokerTraffic.putSize}</if> "
            + "<if test=\"brokerTraffic.getCount != 0\">,#{brokerTraffic.getCount}</if> "
            + "<if test=\"brokerTraffic.getSize != 0\">,#{brokerTraffic.getSize}</if> "
            + ") on duplicate key update ip=ip"
            + "<if test=\"brokerTraffic.putCount != 0\">,put_count=put_count+values(put_count)</if> "
            + "<if test=\"brokerTraffic.putSize != 0\">,put_size=put_size+values(put_size)</if> "
            + "<if test=\"brokerTraffic.getCount != 0\">,get_count=get_count+values(get_count)</if> "
            + "<if test=\"brokerTraffic.getSize != 0\">,get_size=get_size+values(get_size)</if>"
            + "</script>")
    public Integer insert(@Param("brokerTraffic") BrokerTraffic brokerTraffic);
    
    /**
     * 删除记录
     * @param date
     * @return
     */
    @Delete("delete from broker_traffic where create_date < #{createDate}")
    public Integer delete(@Param("createDate") Date date);
    
    /**
     * 获取流量
     * @param ip
     * @param createDate
     * @return
     */
    @Select("select * from broker_traffic where ip=#{ip} and create_date=#{createDate}")
    public List<BrokerTraffic> select(@Param("ip") String ip, @Param("createDate") String createDate);
    
    /**
     * 获取流量
     * @param ip
     * @param createDate
     * @return
     */
    @Select("select cluster_id, create_date, create_time, sum(put_count) put_count, "
            + "sum(put_size) put_size, sum(get_count) get_count, sum(get_size) get_size from broker_traffic "
            + "where create_date=#{createDate} and cluster_id = #{clusterId} "
            + "group by cluster_id, create_date, create_time")
    public List<BrokerTraffic> selectClusterTraffic(@Param("clusterId") int clusterId, 
            @Param("createDate") String createDate);
    
    /**
     * 获取流量
     * @param createDate
     * @param createTimes
     * @param ips
     * @return
     */
    @Select("<script>select ip, cluster_id, sum(put_count) put_count, sum(put_size) put_size, sum(get_count) get_count, sum(get_size) get_size "
            + "from broker_traffic where create_date=#{createDate} "
            + "and create_time in <foreach collection=\"createTimes\" item=\"tm\" separator=\",\" open=\"(\" close=\")\">#{tm}</foreach> "
            + "and ip in <foreach collection=\"ips\" item=\"ip\" separator=\",\" open=\"(\" close=\")\">#{ip}</foreach> "
            + "group by ip</script>")
    public List<BrokerTraffic> selectTrafficList(@Param("createDate") String createDate, 
            @Param("createTimes") List<String> createTimes, 
            @Param("ips") List<String> ips);
    
    /**
     * 获取统计流量
     * @param createDate
     * @param createTimes
     * @param ips
     * @return
     */
    @Select("<script>select max(put_count) put_count, avg(put_count) avg_put_count, "
            + "max(put_size) put_size, avg(put_size) avg_put_size, "
            + "max(get_count) get_count, avg(get_count) avg_get_count, "
            + "max(get_size) get_size, avg(get_size) avg_get_size from "
            + "(select sum(put_count) put_count, sum(put_size) put_size, sum(get_count) get_count, sum(get_size) get_size "
            + "from broker_traffic where create_date=#{createDate} "
            + "and ip in <foreach collection=\"ips\" item=\"ip\" separator=\",\" open=\"(\" close=\")\">#{ip}</foreach> "
            + "and create_time >= #{beginTime} group by create_time) tmp</script>")
    public BrokerTraffic selectTrafficStatistic(@Param("createDate") String createDate, 
            @Param("ips") List<String> ips, @Param("beginTime") String beginTime);
    
    /**
     * 获取统计流量
     * @param createDate
     * @param createTimes
     * @param ips
     * @return
     */
    @Select("select max(put_count) put_count, avg(put_count) avg_put_count, "
            + "max(put_size) put_size, avg(put_size) avg_put_size, "
            + "max(get_count) get_count, avg(get_count) avg_get_count, "
            + "max(get_size) get_size, avg(get_size) avg_get_size "
            + "from broker_traffic where create_date=#{createDate} and ip = #{ip} and create_time >= #{beginTime}")
    public BrokerTraffic selectTrafficStatisticByIp(@Param("createDate") String createDate, 
            @Param("ip") String ip, @Param("beginTime") String beginTime);
}
