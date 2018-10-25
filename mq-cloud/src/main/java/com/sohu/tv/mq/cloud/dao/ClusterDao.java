package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.Cluster;
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
    @Insert("insert into cluster values(#{cluster.id}, #{cluster.name}, #{cluster.vipChannelEnabled}, #{cluster.online})")
    public Integer insert(@Param("cluster")Cluster cluster);
}
