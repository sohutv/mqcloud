package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.BrokerAutoUpdateStep;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * broker自动更新步骤dao
 *
 * @author yongfeigao
 * @date 2024年10月31日
 */
public interface BrokerAutoUpdateStepDao {

    /**
     * 保存记录
     */
    @Insert("<script>insert into broker_auto_update_step(broker_auto_update_id,broker_addr,broker_name,broker_id,broker_base_dir,broker_version,`order`,status,action) values"
            + "<foreach collection=\"steps\" item=\"step\" separator=\",\">"
            + "(#{step.brokerAutoUpdateId},#{step.brokerAddr},#{step.brokerName},#{step.brokerId},#{step.brokerBaseDir},#{step.brokerVersion},#{step.order},#{step.status},#{step.action})"
            + "</foreach></script>")
    public Integer batchInsert(@Param("steps") List<BrokerAutoUpdateStep> step);

    /**
     * 更新
     */
    @Update("<script>update broker_auto_update_step set id = #{id}" +
            "<if test=\"status != 0\">,status=#{status} </if>" +
            "<if test=\"info != null\">,info=#{info} </if>" +
            "<if test=\"startTime != null\">,start_time=#{startTime} </if>" +
            "<if test=\"endTime != null\">,end_time=#{endTime} </if>" +
            "where id = #{id}</script>")
    public Integer update(BrokerAutoUpdateStep step);

    /**
     * 根据brokerAutoUpdateId查询
     */
    @Select("select * from broker_auto_update_step where broker_auto_update_id = #{brokerAutoUpdateId} order by `order`")
    public List<BrokerAutoUpdateStep> selectByBrokerAutoUpdateId(int brokerAutoUpdateId);

    /**
     * 根据brokerAutoUpdateId查询可以执行的
     */
    @Select("<script>select * from broker_auto_update_step where broker_auto_update_id = #{brokerAutoUpdateId} and status in " +
            "<foreach collection='statusArray' item='status' open='(' separator=',' close=')'>#{status}</foreach> " +
            "order by `order` limit 1" +
            "</script>")
    public BrokerAutoUpdateStep selectExecutable(@Param("brokerAutoUpdateId") int brokerAutoUpdateId, @Param("statusArray") int[] statusArray);

    /**
     * 根据id查询
     */
    @Select("select * from broker_auto_update_step where id = #{id}")
    public BrokerAutoUpdateStep selectById(int id);
}
