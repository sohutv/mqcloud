package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import org.apache.ibatis.annotations.*;

import java.util.List;
/**
 * BrokerDao
 * 
 * @author yongfeigao
 * @date 2018年11月14日
 */
public interface BrokerDao {
    /**
     * 查询
     * 
     * @return
     */
    @Select("select * from broker where cid = #{cid}")
    public List<Broker> selectByClusterId(@Param("cid") int cid);

    /**
     * 查询
     * 
     * @return
     */
    @Select("select * from broker order by broker_name, broker_id")
    public List<Broker> selectAll();

    /**
     * 插入
     * 
     * @param notice
     */
    @Insert("<script>insert into broker(cid,addr,broker_name,broker_id"
            + "<if test=\"bk.baseDir != null\">,base_dir</if>,writable"
            + ") values(#{bk.cid},#{bk.addr},#{bk.brokerName},#{bk.brokerID}"
            + "<if test=\"bk.baseDir != null\">,#{bk.baseDir}</if>"
            + ",#{bk.writable})</script>")
    public Integer insert(@Param("bk") Broker broker);
    
    /**
     * 更新
     * 
     * @param notice
     */
    @Update("update broker set check_status = #{checkStatus}, check_time = now() where cid = #{cid} and addr = #{addr}")
    public Integer update(@Param("cid") int cid, @Param("addr") String addr, @Param("checkStatus") int checkStatus);
    
    /**
     * 删除
     * 
     * @param cid
     */
    @Delete("delete from broker where cid=#{cid}")
    public Integer delete(@Param("cid") int cid);

    /**
     * 查询
     *
     * @return
     */
    @Select("select * from broker where cid = #{cid} and addr = #{addr}")
    public Broker selectBroker(@Param("cid") int cid, @Param("addr") String addr);

    /**
     * 更新
     *
     * @param notice
     */
    @Update("update broker set writable = #{writable} where cid = #{cid} and addr = #{addr}")
    public Integer updateWritable(@Param("cid") int cid, @Param("addr") String addr, @Param("writable") int writable);

    /**
     * 重置日流量大小
     */
    @Update("update broker set size_1d = 0, size_2d = 0, size_3d = 0, size_5d = 0, size_7d = 0")
    public Integer resetDayCount();

    /**
     * 更新日流量大小
     */
    @Update("update broker set size_1d = size_1d + #{traffic.size1d}, size_2d = size_2d + #{traffic.size2d}, " +
            "size_3d = size_3d + #{traffic.size3d}, size_5d = size_5d + #{traffic.size5d}, size_7d = size_7d + #{traffic.size7d} where addr = #{traffic.ip}")
    public Integer updateDayCount(@Param("traffic") BrokerTraffic brokerTraffic);
}
