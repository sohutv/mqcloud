package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.Cluster;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Insert("insert into cluster values(#{cluster.id}, #{cluster.name}, #{cluster.vipChannelEnabled}, #{cluster.online}"
            + ", #{cluster.transactionEnabled}, #{cluster.traceEnabled})")
    public Integer insert(@Param("cluster")Cluster cluster);
}
