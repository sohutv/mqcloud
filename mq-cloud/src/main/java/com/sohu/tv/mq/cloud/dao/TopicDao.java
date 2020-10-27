package com.sohu.tv.mq.cloud.dao;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicConsumer;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;

/**
 * topic dao
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
public interface TopicDao {
    /**
     * 插入记录
     * 
     * @param topic
     */
    @Options(useGeneratedKeys = true, keyProperty = "topic.id")
    @Insert("insert into topic(cluster_id, name, queue_num, ordered, create_date, trace_enabled, info, delay_enabled, serializer, traffic_warn_enabled) "
            + "values(#{topic.clusterId},#{topic.name},#{topic.queueNum},#{topic.ordered},now(),#{topic.traceEnabled},"
            + "#{topic.info}, #{topic.delayEnabled}, #{topic.serializer}, #{topic.trafficWarnEnabled})")
    public Integer insert(@Param("topic") Topic topic);
    
    /**
     * 根据id列表批量查询topic
     * @param idList
     * @return List<Topic>
     */
    @Select("<script>select * from topic where id in "
            + "<foreach collection=\"idList\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + "</script>")
    public List<Topic> selectByIdList(@Param("idList") Collection<Long> idList);
    
    /**
     * 根据uid查询topic列表
     * @param idList
     * @return List<Topic>
     */
    @Select("<script>select * from topic t where 1=1 "
            + "<if test=\"uid != 0\"> and id in (select tid from user_producer where uid = #{uid} union select tid from user_consumer where uid = #{uid})"
            + "<if test=\"traceClusterIds.size > 0\"> and cluster_id not in <foreach collection=\"traceClusterIds\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach></if>"
            + "</if>"
            + "<if test=\"topic != null\"> and name like '%${topic}%' </if> "
            + "order by count desc limit #{m},#{n}</script>")
    public List<Topic> selectByUid(@Param("topic") String topic, @Param("uid") long uid, @Param("m") int m, @Param("n") int size, @Param("traceClusterIds") List<Integer> traceClusterIds);
    
    /**
     * 根据uid查询topic列表数量
     * @param idList
     * @return List<Topic>
     */
    @Select("<script>select count(1) from topic t where 1=1 "
            + "<if test=\"uid != 0\"> and id in (select tid from user_producer where uid = #{uid} union select tid from user_consumer where uid = #{uid})"
            + "<if test=\"traceClusterIds.size > 0\"> and cluster_id not in <foreach collection=\"traceClusterIds\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach></if>"
            + "</if>"
            + "<if test=\"topic != null\"> and name like '%${topic}%' </if>"
            + "</script>")
    public Integer selectByUidCount(@Param("topic") String topic, @Param("uid") long uid, @Param("traceClusterIds") List<Integer> traceClusterIds);
    
    /**
     * 根据cluster_id查询topic
     * @param idList
     * @return List<Topic>
     */
    @Select("select * from topic where cluster_id = #{clusterId}")
    public List<Topic> selectByClusterId(@Param("clusterId") int clusterId);

    /**
     * 根据cluster_id查询开启了流量预警功能的topic
     * @param clusterId
     * @return List<Topic>
     */
    @Select("select * from topic where cluster_id = #{clusterId} and traffic_warn_enabled = 1")
    public List<Topic> selectTrafficWarnEnabledTopic(@Param("clusterId") int clusterId);
    
    /**
     * 更新记录
     * 
     * @param topic
     */
    @Update("update topic set queue_num=#{topic.queueNum} where id=#{topic.id}")
    public Integer update(@Param("topic") Topic topic);
    
    /**
     * 更新count
     * 
     * @param topic
     */
    @Update("<script>update topic set count = case id "
            + "<foreach collection=\"topicTrafficList\" item=\"topicTraffic\" separator=\" \">"
            + "when #{topicTraffic.tid} then #{topicTraffic.count}"
            + "</foreach> end where id in"
            + "<foreach collection=\"topicTrafficList\" item=\"tt\" separator=\",\" open=\"(\" close=\")\">#{tt.tid}</foreach>"
            + "</script>")
    public Integer updateCount(@Param("topicTrafficList") List<TopicTraffic> topicTrafficList);
    
    /**
     * 查询所有topic
     * @param idList
     * @return List<Topic>
     */
    @Select("select * from topic order by name")
    public List<Topic> selectAll();
    
    /**
     * 按照名字查询
     * @param name
     * @return Topic
     */
    @Select("select * from topic where name = #{name}")
    public Topic selectByName(@Param("name") String name);
    
    /**
     * 删除topic
     */
    @Delete("delete from topic where id=#{id}")
    public Integer delete(@Param("id") long id);
    
    /**
     * 根据topic name批量查询topic
     * @param idList
     * @return List<Topic>
     */
    @Select("<script>select * from topic where name in "
            + "<foreach collection=\"nameList\" item=\"name\" separator=\",\" open=\"(\" close=\")\">#{name}</foreach>"
            + "</script>")
    public List<Topic> selectByNameList(@Param("nameList") Collection<String> nameList);
    
    /**
     * 获取所有topic的消费者
     * @return
     */
    @Select("SELECT c.id cid,c.name consumer,t.id tid,t.name topic,t.cluster_id FROM consumer c, topic t "
            + "where c.tid = t.id and c.consume_way = #{consume_way}")
    public List<TopicConsumer> selectTopicConsumer(@Param("consume_way") int consumeWay);
    /**
     * 获取指定topic的消费者
     */
    @Select("SELECT c.id cid,c.name consumer,t.id tid, t.name topic FROM topic t, consumer c "
            + "where t.id = #{tid} and c.tid = t.id")
    public List<TopicConsumer> selectTopicConsumerByTid(@Param("tid") long tid);
    
    /**
     * 更新记录
     * 
     * @param tid
     * @param info
     */
    @Update("update topic set info=#{info} where id=#{tid}")
    public Integer updateTopicInfo(@Param("tid") long tid, @Param("info") String info);
    
    /**
     * 更新记录
     * 
     * @param tid
     * @param info
     */
    @Update("update topic set trace_enabled=#{traceEnabled} where id=#{tid}")
    public Integer updateTopicTrace(@Param("tid") long tid, @Param("traceEnabled") int traceEnabled);
    
    /**
     * 重置count
     * 
     * @param day
     */
    @Update("update topic set count = 0 where count > 0 and update_time < DATE_SUB(CURDATE(),INTERVAL #{dayAgo} DAY)")
    public Integer resetCount(@Param("dayAgo") int dayAgo);

    /**
     * 更新记录
     * @param tid
     * @param trafficWarnEnabled
     */
    @Update("update topic set traffic_warn_enabled=#{trafficWarnEnabled} where id=#{tid}")
    public Integer updateTopicTrafficWarn(@Param("tid") long tid, @Param("trafficWarnEnabled") int trafficWarnEnabled);
} 
