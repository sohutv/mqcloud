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
    @Select("<script>select sum(IFNULL(count,0)) count,sum(size) size from consumer_traffic where create_date=#{createDate,jdbcType=DATE} and consumer_id in "
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

    /**
     * 获取时间段内流量之和
     *
     * @param dateTime
     * @param minutesTimes
     * @param tidList
     * @return
     */
    @Select("<script>select consumer.tid consumerId,sum(IFNULL(traffic.count,0)) count from consumer "
            + "left join consumer_traffic traffic on consumer.id = traffic.consumer_id "
            + "where consumer.tid in"
            + "<foreach collection=\"tidList\" item=\"tid\" separator=\",\" open=\"(\" close=\")\">#{tid}</foreach>"
            + "and traffic.create_time in "
            + "<foreach collection=\"minutesTimes\" item=\"minus\" separator=\",\" open=\"(\" close=\")\">#{minus}</foreach>"
            + "and traffic.create_date = #{dateTime,jdbcType=DATE} "
            + "group by consumer.tid"
            + "</script>")
    List<ConsumerTraffic> selectFlowByDateTimeRange(@Param("dateTime") Date dateTime, @Param("minutesTimes") List<String> minutesTimes,
                                                 @Param("tidList") List<Long> tidList);

    /**
     * 依据createTime时间范围和cid进行流量求和
     */
    @Select("<script>select consumer_id,sum(IFNULL(count,0)) count from consumer_traffic "
            + "where consumer_id in "
            + "<foreach collection=\"cidList\" item=\"tid\" separator=\",\" open=\"(\" close=\")\">#{tid}</foreach> "
            + "and create_time in "
            + "<foreach collection=\"minutesTimes\" item=\"minus\" separator=\",\" open=\"(\" close=\")\">#{minus}</foreach> "
            + "and create_date = #{dateTime,jdbcType=DATE} "
            + "group by consumer_id"
            + "</script>")
    List<ConsumerTraffic> selectFlowByDateTimeRangeAndCids(@Param("dateTime") Date dateTime, @Param("minutesTimes") List<String> minutesTimes,
                                              @Param("cidList") List<Long> tidList);

    /**
     * 依据createTime时间范围和tid进行流量求和
     */
    @Select("<script>select sum(IFNULL(count,0)) count from consumer_traffic where consumer_id in "
            + "<foreach collection=\"cids\" item=\"cid\" separator=\",\" open=\"(\" close=\")\">#{cid}</foreach>"
            + "and create_date BETWEEN #{startTime,jdbcType=DATE} and #{endTime,jdbcType=DATE} "
            + "</script>")
    Long selectSummaryDataByRangeTime(@Param("cids")List<Long> cids, @Param("startTime")Date startTime,
                                      @Param("endTime")Date endTime);

    /**
     * 查找当日无消费量消费者ID
     * @param idList
     * @return
     */
    @Select("<script>"
            + "SELECT cons.id "
            + "FROM consumer cons LEFT JOIN consumer_traffic traffic  "
            + "ON cons.id = traffic.consumer_id and traffic.create_date = #{createDate,jdbcType=DATE} "
            + "WHERE cons.tid IN "
            + "<foreach collection=\"tids\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + "GROUP BY cons.id  "
            + "HAVING sum(IFNULL( traffic.count, 0) ) = 0  "
            + "</script>")
    List<Long> selectNoneConsumerFlowsId(@Param("tids") List<Long> idList,@Param("createDate") Date createDate);
}
