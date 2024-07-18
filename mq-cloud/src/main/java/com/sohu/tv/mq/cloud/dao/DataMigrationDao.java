package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.DataMigration;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 数据迁移
 *
 * @author yongfeigao
 * @date 2024年07月03日
 */
public interface DataMigrationDao {
    /**
     * 插入
     */
    @Options(useGeneratedKeys = true, keyProperty = "dm.id")
    @Insert("insert into data_migration(source_ip,source_path,dest_ip,dest_path,data_count) values(#{dm.sourceIp},#{dm.sourcePath},#{dm.destIp},#{dm.destPath},#{dm.dataCount})")
    public Long insert(@Param("dm") DataMigration dataMigration);

    /**
     * 更新
     */
    @Update("update data_migration set status = #{dm.status},cost_time=#{dm.costTime},info=#{dm.info} where id = #{dm.id}")
    public Integer update(@Param("dm") DataMigration dataMigration);

    /**
     * 查询
     */
    @Select("select * from data_migration order by id desc")
    public List<DataMigration> selectAll();

    /**
     * 查询
     */
    @Select("select * from data_migration where status = #{status} order by id desc")
    public List<DataMigration> selectByStatus(@Param("status") int status);

    /**
     * 查询
     */
    @Select("select * from data_migration where id = #{id}")
    public DataMigration selectById(@Param("id") long id);
}
