package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.Cluster;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
/**
 * 集群dao
 * 
 * @author yongfeigao
 * @date 2018年10月10日
 */
public interface ClusterDao {
    /**
     * 查询
     */
    @Select("select * from cluster")
    public List<Cluster> select();
    
    /**
     * 保存数据
     */
    @Insert("insert into cluster(id, name, vip_channel_enabled, online, transaction_enabled, trace_enabled, status) " +
            "values(#{cluster.id}, #{cluster.name}, #{cluster.vipChannelEnabled}, #{cluster.online}, "
            + "#{cluster.transactionEnabled}, #{cluster.traceEnabled}, #{cluster.status})")
    public Integer insert(@Param("cluster")Cluster cluster);

    /**
     * 根据id查询
     */
    @Select("select * from cluster where id = #{id}")
    public Cluster selectById(@Param("id") int id);

    /**
     * 更新
     */
    @Update("update cluster set status = #{status} where id = #{id}")
    public Integer updateStatus(@Param("id") int id, @Param("status") int status);
}
