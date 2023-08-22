package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.Broker;
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
    @Select("select * from broker")
    public List<Broker> selectAll();

    /**
     * 插入
     * 
     * @param notice
     */
    @Insert("<script>insert into broker(cid,addr,broker_name,broker_id"
            + "<if test=\"bk.baseDir != null\">,base_dir</if>"
            + ") values(#{bk.cid},#{bk.addr},#{bk.brokerName},#{bk.brokerID}"
            + "<if test=\"bk.baseDir != null\">,#{bk.baseDir}</if>"
            + ")</script>")
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
}
