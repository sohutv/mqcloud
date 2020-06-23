package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.BrokerConfig;
/**
 * broker配置
 * 
 * @author yongfeigao
 * @date 2020年5月18日
 */
public interface BrokerConfigDao {
    /**
     * 保存
     */
    @Insert("insert into broker_config(`gid`, `key`, `value`, `desc`, `tip`, `order`, `dynamic_modify`, `option`, `required`) "
            + "values(#{brokerConfig.gid}, #{brokerConfig.key}, #{brokerConfig.value}, #{brokerConfig.desc},"
            + "#{brokerConfig.tip}, #{brokerConfig.order}, #{brokerConfig.dynamicModify}, #{brokerConfig.option}, #{brokerConfig.required})")
    public Integer insert(@Param("brokerConfig") BrokerConfig brokerConfig);

    /**
     * 删除
     */
    @Delete("delete from broker_config where `id` = #{id}")
    public Integer delete(@Param("id") int id);

    /**
     * 更新
     */
    @Update("update broker_config set `key`=#{brokerConfig.key}, `value`=#{brokerConfig.value}, `desc`=#{brokerConfig.desc}, "
            + "`tip`=#{brokerConfig.tip}, `order`=#{brokerConfig.order}, `dynamic_modify`=#{brokerConfig.dynamicModify}, "
            + "`option`=#{brokerConfig.option}, gid=#{brokerConfig.gid}, `required`= #{brokerConfig.required} where id = #{brokerConfig.id}")
    public Integer update(@Param("brokerConfig") BrokerConfig brokerConfig);

    /**
     * 查询
     */
    @Select("select * from broker_config where gid = #{gid} order by `order`")
    public List<BrokerConfig> select(@Param("gid") int gid);
    
    /**
     * 查询
     */
    @Select("select * from broker_config where id = #{id}")
    public BrokerConfig selectById(@Param("id") int id);
    
    /**
     * 查询
     */
    @Select("select * from broker_config order by `order`")
    public List<BrokerConfig> selectAll();
    
    /**
     * 查询
     */
    @Select("select broker_config.* from broker_config, cluster_config where cluster_config.cid = #{cid} and cluster_config.bid = broker_config.id")
    public List<BrokerConfig> selectByCid(@Param("cid") int cid);
}
