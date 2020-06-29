package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.BrokerConfigGroup;

/**
 * broker配置组
 * 
 * @author yongfeigao
 * @date 2020年5月18日
 */
public interface BrokerConfigGroupDao {

    /**
     * 保存
     */
    @Insert("insert into broker_config_group(`group`, `order`) values(#{group}, #{order})")
    public Integer insert(@Param("group") String group, @Param("order") int order);

    /**
     * 删除
     */
    @Delete("delete from broker_config_group where id = #{id}")
    public Integer delete(@Param("id") int id);

    /**
     * 更新
     */
    @Update("update broker_config_group set `group` = #{group.group}, `order` = #{group.order} where id = #{group.id}")
    public Integer update(@Param("group") BrokerConfigGroup brokerConfigGroup);

    /**
     * 查询
     */
    @Select("select * from broker_config_group order by `order`")
    public List<BrokerConfigGroup> select();
}
