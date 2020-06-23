package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.ClusterConfig;
/**
 * 集群配置
 * 
 * @author yongfeigao
 * @date 2020年5月18日
 */
public interface ClusterConfigDao {
    /**
     * 保存
     */
    @Insert("insert into cluster_config(`cid`, `bid`, `online_value`) "
            + "values(#{clusterConfig.cid}, #{clusterConfig.bid}, #{clusterConfig.onlineValue})")
    public Integer insert(@Param("clusterConfig") ClusterConfig clusterConfig);

    /**
     * 删除
     */
    @Delete("delete from cluster_config where `cid` = #{cid} and `bid` = #{bid}")
    public Integer delete(@Param("cid") int cid, @Param("bid") int bid);

    /**
     * 更新
     */
    @Update("update cluster_config set `online_value`=#{clusterConfig.onlineValue} where cid = #{clusterConfig.cid} and bid = #{clusterConfig.bid}")
    public Integer update(@Param("clusterConfig") ClusterConfig clusterConfig);

    /**
     * 查询
     */
    @Select("select * from cluster_config where cid = #{cid}")
    public List<ClusterConfig> select(@Param("cid") int cid);
}
