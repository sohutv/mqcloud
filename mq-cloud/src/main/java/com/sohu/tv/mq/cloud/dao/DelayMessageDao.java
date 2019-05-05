package com.sohu.tv.mq.cloud.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
/**
 * 延迟消息
 * 
 * @author zhehongyuan
 * @date 2019年4月23日
 */
public interface DelayMessageDao {
    
    /**
     * 延迟消息的TopicTraffic记录
     */
    @Select("select create_time ,sum(count) as count from producer_total_stat where"
            + " producer in(select distinct producer from user_producer where tid=#{tid}) "
            + "and create_date = #{createDate} group by create_time")
    public List<TopicTraffic> selectTopicTraffic(@Param("tid")long tid, @Param("createDate")int createDate);
    
    
    /**
     * 获取topic流量
     * 
     * @param tid
     * @param createDate
     * @return
     */
    @Select("select #{tid} as tid, create_time, sum(count) as count from producer_total_stat where"
            + " producer in(select distinct producer from user_producer where tid=#{tid}) "
            + "and create_date = #{createDate} and create_time = #{createTime} group by create_time")
    public TopicTraffic selectByIdListDateTime(@Param("tid") long tid,
            @Param("createDate") int createDate, @Param("createTime") String createTime);
    
    /**
     * 获取topic日流量
     * @param tid
     * @param createDate
     * @return
     */
    @Select("select #{tid} as tid, sum(count) as count from producer_total_stat where"
            + " producer in(select distinct producer from user_producer where tid=#{tid}) "
            + "and create_date = #{createDate}")
    public TopicTraffic selectTotalTraffic(@Param("tid") long tid, @Param("createDate") int createDate);
    
}
