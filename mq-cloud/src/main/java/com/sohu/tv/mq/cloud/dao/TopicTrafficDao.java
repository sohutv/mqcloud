package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * topic流量
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月26日
 */
public interface TopicTrafficDao {
    /**
     * 插入记录
     * 
     * @param topic
     */
    @Insert("insert into topic_traffic(tid, create_date, create_time, count, size) values("
            + "#{topicTraffic.tid},#{topicTraffic.createDate},#{topicTraffic.createTime},#{topicTraffic.count},#{topicTraffic.size})")
    public void insert(@Param("topicTraffic") TopicTraffic topicTraffic);
    
    /**
     * 删除记录
     * @param date
     * @return
     */
    @Delete("delete from topic_traffic where create_date < #{createDate,jdbcType=DATE}")
    public Integer delete(@Param("createDate") Date date);
    
    /**
     * 获取topic流量
     * @param tid
     * @param createDate
     * @return
     */
    @Select("select * from topic_traffic where tid=#{tid} and create_date=#{createDate,jdbcType=DATE}")
    public List<TopicTraffic> select(@Param("tid") long tid, @Param("createDate") Date createDate);
    
    /**
     * 获取topic日流量
     * @param tid
     * @param createDate
     * @return
     */
    @Select("select #{tid} as tid,sum(count) count,sum(size) size from topic_traffic where tid=#{tid} and create_date=#{createDate,jdbcType=DATE}")
    public TopicTraffic selectTotalTraffic(@Param("tid") long tid, @Param("createDate") Date createDate);

    /**
     * 获取topic流量
     * 
     * @param tid
     * @param createDate
     * @return
     */
    @Select("<script>select * from topic_traffic where create_date=#{createDate,jdbcType=DATE} and create_time = #{createTime} and tid in "
            + "<foreach collection=\"idList\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + "</script>")
    public List<TopicTraffic> selectByIdListDateTime(@Param("idList") List<Long> idList,
            @Param("createDate") Date createDate, @Param("createTime") String createTime);
    
    /**
     * 获取topic某一时间段的流量
     * @param createDate
     * @param createTimeList
     * @return
     */
    @Select("<script>select tid, sum(count) count, sum(size) size from `topic_traffic` where create_date = #{createDate,jdbcType=DATE} "
            + "and create_time in "
            + "<foreach collection=\"createTimeList\" item=\"time\" separator=\",\" open=\"(\" close=\")\">#{time}</foreach>"
            + " group by tid</script>")
    public List<TopicTraffic> selectByDateTime(@Param("createDate") Date createDate, @Param("createTimeList") List<String> createTimeList);

    /**
     * 获取topic指定日期内的流量信息
     */
    @Select("select tid, create_date createDate, count from topic_traffic where tid = #{tid} and create_date < #{createDate,jdbcType=DATE}")
    public List<TopicTraffic> selectRangeTraffic(@Param("tid") long tid, @Param("createDate") Date createDate);

    /**
     * 根据具体date和time列表查询
     */
    @Select("<script>select * from `topic_traffic` where tid = #{tid} and create_date = #{createDate,jdbcType=DATE} and create_time in "
            + "<foreach collection=\"createTimeList\" item=\"time\" separator=\",\" open=\"(\" close=\")\">#{time}</foreach>"
            + " order by create_time</script>")
    public List<TopicTraffic> selectByCreateDateAndTime(@Param("tid") long tid,
           @Param("createDate") Date createDate, @Param("createTimeList") List<String> createTimeList);

    /**
     * 依据createTime时间范围和tid进行流量求和
     */
    @Select("<script>select sum(IFNULL(count,0)) count from topic_traffic where "
            + "tid = #{tid} "
            + "and create_date BETWEEN #{startTime,jdbcType=DATE} and #{endTime,jdbcType=DATE} "
            + "</script>")
    Long selectSummaryDataByRangeTime(@Param("tid")long tid,@Param("startTime")Date startTime,
                                      @Param("endTime")Date endTime);

    /**
     * 依据createTime时间范围和tid进行流量求和
     */
    @Select("<script>" +
            "SELECT topic.id FROM topic " +
            "left join topic_traffic traffic " +
            "on topic.id = traffic.tid and traffic.create_date = #{creatDay,jdbcType=DATE} " +
            "GROUP BY topic.id " +
            "HAVING sum(IFNULL(traffic.count,0)) = 0 "
            + "</script>")
    List<Long> selectCurrentMsgNum(@Param("creatDay")Date creatDay);

    /**
     * 获取某段时间流量字节大小
     */
    @Select("select tid,create_date,sum(size) size from topic_traffic where "
            + "create_date BETWEEN #{startDate,jdbcType=DATE} and #{endDate,jdbcType=DATE} "
            + "group by tid,create_date limit #{offset},#{size}")
    List<TopicTraffic> selectSummarySize(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("offset") int offset, @Param("size") int size);

    /**
     * 获取某段时间流量字节大小
     */
    @Select("select sum(size) size from topic_traffic where tid = #{tid} and "
            + "create_date BETWEEN #{startDate,jdbcType=DATE} and #{endDate,jdbcType=DATE}")
    Long selectTopicSummarySize(@Param("tid")long tid, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
