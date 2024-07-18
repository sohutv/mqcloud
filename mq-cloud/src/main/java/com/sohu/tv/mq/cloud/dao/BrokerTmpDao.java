package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.Broker;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * BrokerTmpDao
 *
 * @author yongfeigao
 * @date 2024年07月11日
 */
public interface BrokerTmpDao {
    /**
     * 查询
     */
    @Select("select * from broker_tmp where cid = #{cid}")
    public List<Broker> selectByClusterId(@Param("cid") int cid);

    /**
     * 插入
     */
    @Insert("insert into broker_tmp(cid,addr,broker_name,broker_id,base_dir)" +
            " values(#{bk.cid},#{bk.addr},#{bk.brokerName},#{bk.brokerID},#{bk.baseDir})")
    public Integer insert(@Param("bk") Broker broker);

    /**
     * 删除
     */
    @Delete("delete from broker_tmp where cid=#{cid} and addr=#{addr}")
    public Integer delete(@Param("cid") int cid, @Param("addr") String addr);
}
