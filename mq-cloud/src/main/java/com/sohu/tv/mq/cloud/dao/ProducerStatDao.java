package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.ProducerStat;
/**
 * 生产者统计dao
 * 
 * @author yongfeigao
 * @date 2018年9月12日
 */
public interface ProducerStatDao {
    /**
     * 插入记录
     */
    @Insert("<script>insert into producer_stat(total_id,broker,max,avg,count,exception) values "
            + "<foreach collection=\"list\" item=\"s\" separator=\",\">"
            + "(#{s.totalId},#{s.broker},#{s.max},#{s.avg},#{s.count},#{s.exception})"
            + "</foreach>"
            + "</script>")
    public Integer insert(@Param("list")List<ProducerStat> producerStatList);
    
    /**
     * 根据日期查询ProducerStat记录
     */
    @Select("select * from producer_stat where total_id in "
            + "(select id from producer_total_stat where create_date = #{createDate} and producer = #{producer})")
    public List<ProducerStat> selectByDate(@Param("producer")String producer, @Param("createDate")int createDate);
    
    /**
     * 根据id查询ProducerStat记录
     */
    @Select("select * from producer_stat where total_id = #{totalId}")
    public List<ProducerStat> selectById(@Param("totalId")long totalId);
    
    /**
     * 删除
     * @return
     */
    @Delete("delete from producer_stat where total_id in (select id from producer_total_stat where create_date=#{createDate})")
    public Integer delete(@Param("createDate")int createDate);
}
