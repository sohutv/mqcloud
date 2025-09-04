package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.*;
import org.apache.ibatis.annotations.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;

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
    @Insert("insert into topic(cluster_id, name, queue_num, ordered, create_date, trace_enabled, info, msg_type, serializer, traffic_warn_enabled) "
            + "values(#{topic.clusterId},#{topic.name},#{topic.queueNum},#{topic.ordered},now(),#{topic.traceEnabled},"
            + "#{topic.info}, #{topic.msgType}, #{topic.serializer}, #{topic.trafficWarnEnabled})")
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
            + "<if test=\"topic != null\"> and name like #{topic} </if> "
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
            + "<if test=\"topic != null\"> and name like #{topic} </if>"
            + "</script>")
    public Integer selectByUidCount(@Param("topic") String topic, @Param("uid") long uid, @Param("traceClusterIds") List<Integer> traceClusterIds);
    
    /**
     * 根据uid查询topic状况
     * @param uid
     * @param traceClusterIds
     * @return
     */
    @Select("<script>select count(1) size, sum(count) count from topic t where 1=1 "
            + "<if test=\"uid != 0\"> and id in (select tid from user_producer where uid = #{uid} union select tid from user_consumer where uid = #{uid})"
            + "<if test=\"traceClusterIds.size > 0\"> and cluster_id not in <foreach collection=\"traceClusterIds\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach></if>"
            + "</if>"
            + "</script>")
    public TopicStat selectTopicStat(@Param("uid") long uid, @Param("traceClusterIds") List<Integer> traceClusterIds);
    
    /**
     * 根据cluster_id查询topic
     * @param idList
     * @return List<Topic>
     */
    @Select("select * from topic where cluster_id = #{clusterId}")
    public List<Topic> selectByClusterId(@Param("clusterId") int clusterId);

    /**
     * 根据cluster_id查询顺序topic
     */
    @Select("select name from topic where cluster_id = #{clusterId} and ordered = 1")
    public List<String> selectOrderedTopic(@Param("clusterId") int clusterId);

    /**
     * 根据cluster_id查询开启了流量突增预警功能的topic
     * @param clusterId
     * @return List<Topic>
     */
    @Select("select * from topic where cluster_id = #{clusterId} and traffic_warn_enabled = 1")
    public List<Topic> selectTrafficWarnEnabledTopic(@Param("clusterId") int clusterId);
    
    /**
     * 更新count
     */
    @Update("update topic set count = #{topicTraffic.count}, size = #{topicTraffic.size}  where id = #{topicTraffic.tid}")
    public Integer updateCount(@Param("topicTraffic") TopicTraffic topicTraffic);
    
    /**
     * 查询所有topic
     * @param idList
     * @return List<Topic>
     */
    @Select("select * from topic order by name")
    public List<Topic> selectAll();


    /**
     * 查询所有topic以及对应的生产者
     * @return Cursor<Topic>
     */
    @Select("<script> select DISTINCT topic.*, user_producer.producer as producerName " +
            "from topic inner join user_producer " +
            "on topic.id = user_producer.tid " +
            "left join client_language on client_language.client_group_name = user_producer.producer " +
            "where client_language.tid is null " +
            " <if test=\"topicName != null and topicName != ''\"> and topic.name = #{topicName} </if>"+
            "</script>")
    public List<Topic> selectAllWithProducer(@Param("topicName") String topicName);


    /**
     * 查询所有topic以及对应的消费者
     * @return Cursor<Topic>
     */
    @Select("<script> select DISTINCT topic.*, consumer.name as consumerName " +
            "from topic inner join consumer " +
            "on topic.id = consumer.tid " +
            "left join client_language on client_language.client_group_name = consumer.name " +
            "where client_language.tid is null " +
            " <if test=\"topicName != null and topicName != ''\"> and topic.name = #{topicName} </if>"+
            "</script>")
    public List<Topic> selectAllWithConsumer(@Param("topicName") String topicName);
    
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
     * @param topic
     */
    @Update("<script>update topic set id=#{topic.id} "
            + "<if test=\"topic.info != null\">,info=#{topic.info}</if>"
            + "<if test=\"topic.queueNum != 0\">,queue_num=#{topic.queueNum}</if>"
            + "<if test=\"topic.clusterId != 0\">,cluster_id=#{topic.clusterId}</if>"
            + "where id=#{topic.id}</script>")
    public Integer update(@Param("topic") Topic topic);

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
    @Update("update topic set count = 0, size = 0 where update_time < #{dayAgo}")
    public Integer resetCount(@Param("dayAgo") Date dayAgo);

    /**
     * 更新记录
     * @param tid
     * @param trafficWarnEnabled
     */
    @Update("update topic set traffic_warn_enabled=#{trafficWarnEnabled} where id=#{tid}")
    public Integer updateTopicTrafficWarn(@Param("tid") long tid, @Param("trafficWarnEnabled") int trafficWarnEnabled);

    /**
     * 查询非trace topic
     * @return List<Topic>
     */
    @Select("select * from topic t where cluster_id not in (select id from cluster where trace_enabled = 1)")
    public List<Topic> selectNoneTraceableTopic();


    /**
     * 依据条件查询topic总数
     * @param limitTids topic uid,gid限制条件
     * @param cid 集群id
     */
    @Select("<script>select count(id) from topic where id in "
            + "<foreach collection=\"limitTids\" item=\"tid\" separator=\",\" open=\"(\" close=\")\">#{tid}</foreach>"
            +" <if test=\"cid != null\"> and cluster_id = #{cid} </if>"
            + "</script>")
    int queryTopicCountByLimit(@Param("limitTids") List<Long> limitTids, @Param("cid") Long cid);


    /**
     * 依据条件分页查询topic
     * @param limitTids topic uid,gid限制条件
     */
    @Select("<script>select * from topic " +
            "<if test=\"limitTids != null\"> "
            + "where id in "
            + "<foreach collection=\"limitTids\" item=\"tid\" separator=\",\" open=\"(\" close=\")\">#{tid}</foreach> "
            + "</if> "
            + "order by count desc "
            + "</script>")
    List<Topic> queryTopicDataByLimit(@Param("limitTids") List<Long> limitTids);

    /**
     * 返回没有消费者的主题
     */
    @Select("<script>" +
            "select topic.id\n" +
            "from topic\n" +
            "left join consumer on topic.id = consumer.tid\n" +
            "where consumer.id is null"
            + "</script>")
    List<Long> selectNoMatchTids();

    /**
     * 返回正常匹配消费者的主题
     */
    @Select("<script>" +
            "select topic.id\n" +
            "from topic\n" +
            "inner join consumer on topic.id = consumer.tid\n"
            + "</script>")
    List<Long> selectActiveMatchTids();

    /**
     * 确认主题状态
     */
    @Update("update topic set effective= 1 where id=#{tid}")
    void updateCheckStatus(@Param("tid") long tid);

    /**
     * 获取所有的排序后的Tids
     */
    @Select("<script>" +
            "select id " +
            "from topic " +
            "where 1=1 "+
            " <if test=\"cid != null\"> and cluster_id = #{cid} </if>"+
            "order by count desc "
            + "</script>")
    List<Long> selectAllTidsByCid(@Param("cid") Long cid);

    /**
     * 查询http消费的消费者
     */
    @Select("select topic.name topic, consumer.name consumer, consumer.consume_way consumeWay from topic,consumer where consumer.protocol = 1 and consumer.tid = topic.id")
    List<TopicConsumer> selectHttpTopicConsumer();


    /**
     * 查询http生产者
     */
    @Select("select distinct topic.name topic, user_producer.producer producer from topic,user_producer where user_producer.protocol = 1 and user_producer.tid = topic.id")
    List<TopicProducer> selectHttpTopicProducer();

    /**
     * 重置日流量大小
     */
    @Update("update topic set size_1d = 0, size_2d = 0, size_3d = 0, size_5d = 0, size_7d = 0, count_1d = 0, count_2d = 0")
    public Integer resetDayCount();

    /**
     * 更新日流量大小
     */
    @Update("update topic set size_1d = size_1d + #{topicTraffic.size1d}, size_2d = size_2d + #{topicTraffic.size2d}, " +
            "size_3d = size_3d + #{topicTraffic.size3d}, size_5d = size_5d + #{topicTraffic.size5d}, " +
            "size_7d = size_7d + #{topicTraffic.size7d}, " +
            "count_1d = count_1d + #{topicTraffic.count1d}, count_2d = count_2d + #{topicTraffic.count2d} " +
            "where id = #{topicTraffic.tid}")
    public Integer updateDayCount(@Param("topicTraffic") TopicTraffic topicTraffic);

    /**
     * 根据cluster_id查询topic
     *
     * @param idList
     * @return List<Topic>
     */
    @Select("select * from topic where cluster_id = #{clusterId} order by size_1d desc limit #{n}")
    public List<Topic> selectTopNSizeTopic(@Param("clusterId") int clusterId, @Param("n") int n);
}
