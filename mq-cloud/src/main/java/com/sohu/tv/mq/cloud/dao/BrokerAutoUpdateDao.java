package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdate;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * broker自动更新dao
 *
 * @author yongfeigao
 * @date 2024年10月31日
 */
public interface BrokerAutoUpdateDao {

    /**
     * 保存记录
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into broker_auto_update(cid, status) values(#{cid}, #{status})")
    public Integer insert(BrokerAutoUpdate brokerAutoUpdate);

    /**
     * 更新状态
     */
    @Update("<script>update broker_auto_update set status = #{status} " +
            "<if test=\"startTime != null\">,start_time=#{startTime} </if>" +
            "where id = #{id}</script>")
    public Integer update(BrokerAutoUpdate brokerAutoUpdate);

    /**
     * 根据cid查询
     */
    @Select("select * from broker_auto_update where cid = #{cid} order by id desc")
    public List<BrokerAutoUpdate> selectByCid(int cid);

    /**
     * 根据cid查询未完成的
     */
    @Select("<script>select * from broker_auto_update where cid = #{cid} and status in " +
            "<foreach collection='statusArray' item='status' open='(' separator=',' close=')'>#{status}</foreach>" +
            "</script>")
    public List<BrokerAutoUpdate> selectUndoneByCid(@Param("cid") int cid, @Param("statusArray") int[] statusArray);

    /**
     * 查询未可执行的
     */
    @Select("<script>select * from broker_auto_update where status in " +
            "<foreach collection='statusArray' item='status' open='(' separator=',' close=')'>#{status}</foreach>" +
            "</script>")
    public List<BrokerAutoUpdate> selectExecutable(@Param("statusArray") int[] statusArray);

    /**
     * 根据id查询
     */
    @Select("select * from broker_auto_update where id = #{id}")
    public BrokerAutoUpdate selectById(int id);
}
