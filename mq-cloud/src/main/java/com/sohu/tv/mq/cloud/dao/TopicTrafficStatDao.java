package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.TopicTrafficStat;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * topic流量统计dao
 * @author yongweizhao
 * @create 2020/8/12 10:18
 */
public interface TopicTrafficStatDao {

    /**
     * 插入数据
     */
    @Update("insert into topic_traffic_stat(tid, avg_max, max_max, days) values(" +
            "#{topicTrafficStat.tid},#{topicTrafficStat.avgMax},#{topicTrafficStat.maxMax},#{topicTrafficStat.days}) " +
            "on duplicate key update avg_max=values(avg_max), max_max=values(max_max), days=values(days)")
    public void insertAndUpdate(@Param("topicTrafficStat") TopicTrafficStat topicTrafficStat);

    /**
     * 根据tid查找
     */
    @Select("select * from topic_traffic_stat where tid = #{tid}")
    public TopicTrafficStat select(@Param("tid") long tid);

    /**
     * 获取所有tid
     */
    @Select("select tid from topic_traffic_stat")
    public List<Long> selectAllTid();

    /**
     * 删除
     */
    @Delete("<script>delete from topic_traffic_stat "
            + "where tid in "
            + "<foreach collection=\"idList\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + "</script>")
    public Integer delete(@Param("idList") List<Long> idList);
}
