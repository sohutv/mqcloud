package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.ClientVersion;
/**
 * 客户端上报的版本
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月31日
 */
public interface ClientVersionDao {
    /**
     * 保存记录
     */
    @Insert("insert into client_version(topic, client, role, version, create_date) "
            + "values(#{cv.topic},#{cv.client},#{cv.role},#{cv.version},now()) "
            + "on duplicate key update version=values(version), update_time = now()")
    public Integer insert(@Param("cv") ClientVersion clientVersion);

    /**
     * 查询
     */
    @Select("select * from client_version order by update_time desc")
    public List<ClientVersion> selectAll();
    
    /**
     * 查询某个client
     */
    @Select("select * from client_version where topic = #{topic} and role = #{role} and client = #{client}")
    public ClientVersion selectClientVersion(@Param("topic") String topic, @Param("client") String client, @Param("role") int role);
}
