package com.sohu.tv.mq.cloud.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.ConsumerTraffic;

/**
 * 消费者流量
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月26日
 */
public interface ConsumerTrafficDao {
    /**
     * 插入记录
     * 
     * @param topic
     */
    @Insert("insert into consumer_traffic(consumer_id, create_date, create_time, count, size) values("
            + "#{traffic.consumerId},now(),#{traffic.createTime},#{traffic.count},#{traffic.size})")
    public void insert(@Param("traffic") ConsumerTraffic consumerTraffic);
    
    /**
     * 删除记录
     * @param date
     * @return
     */
    @Delete("delete from consumer_traffic where create_date < #{createDate}")
    public Integer delete(@Param("createDate")Date date);
    
    /**
     * 获取consumer流量
     * @param tid
     * @param createDate
     * @return
     */
    @Select("select * from consumer_traffic where consumer_id=#{consumerId} and create_date=#{createDate,jdbcType=DATE}")
    public List<ConsumerTraffic> select(@Param("consumerId") long consumerId, @Param("createDate") Date createDate);
    
    /**
     * 获取consumer日流量
     * @param tid
     * @param createDate
     * @return
     */
    @Select("<script>select sum(count) count,sum(size) size from consumer_traffic where create_date=#{createDate,jdbcType=DATE} and consumer_id in "
            + "<foreach collection=\"idList\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + "</script>")
    public ConsumerTraffic selectTotalTraffic(@Param("idList") List<Long> idList, @Param("createDate") Date createDate);

    /**
     * 获取consumer流量
     * 
     * @param tid
     * @param createDate
     * @return
     */
    @Select("<script>select * from consumer_traffic where create_date=#{createDate,jdbcType=DATE} and consumer_id in "
            + "<foreach collection=\"idList\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + "</script>")
    public List<ConsumerTraffic> selectByIdList(@Param("idList") List<Long> idList,
            @Param("createDate") Date createDate);

    /**
     * 获取consumer流量
     * 
     * @param tid
     * @param createDate
     * @return
     */
    @Select("<script>select * from consumer_traffic "
            + "where create_date=#{createDate,jdbcType=DATE} and create_time = #{createTime} and consumer_id in  "
            + "<foreach collection=\"idList\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + "</script>")
    public List<ConsumerTraffic> selectByIdListDateTime(@Param("idList") List<Long> idList,
            @Param("createDate") Date createDate, @Param("createTime") String createTime);
}
