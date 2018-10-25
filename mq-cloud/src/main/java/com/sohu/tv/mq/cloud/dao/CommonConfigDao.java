package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.CommonConfig;

/**
 * 通用配置
 * 
 * @author yongfeigao
 * @date 2018年10月16日
 */
public interface CommonConfigDao {
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from common_config")
    public List<CommonConfig> select();
    
    /**
     * 保存记录
     * @param consumer
     */
    @Insert("replace into common_config(id, `key`, `value`, `comment`) values "
            + "(#{config.id},#{config.key},#{config.value},#{config.comment})")
    public Integer insert(@Param("config")CommonConfig commonConfig);
}
