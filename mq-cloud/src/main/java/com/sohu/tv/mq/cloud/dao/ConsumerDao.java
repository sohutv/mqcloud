package com.sohu.tv.mq.cloud.dao;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.Consumer;

/**
 * consumer dao
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
public interface ConsumerDao {
    /**
     * 插入记录
     * 
     * @param consumer
     */
    @Options(useGeneratedKeys = true, keyProperty = "consumer.id")
    @Insert("insert into consumer(tid, name, consume_way, create_date, trace_enabled, info) values("
            + "#{consumer.tid},#{consumer.name},#{consumer.consumeWay},now(),#{consumer.traceEnabled},#{consumer.info})")
    public Integer insert(@Param("consumer") Consumer consumer);
            
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from consumer where tid = #{tid}")
    public List<Consumer> selectByTid(@Param("tid") long tid);
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from consumer")
    public List<Consumer> selectAll();
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from consumer where id = #{id}")
    public Consumer selectById(@Param("id") long id);
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from consumer where name = #{name}")
    public Consumer selectByName(@Param("name") String name);
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from consumer where id in (select consumer_id from user_consumer where uid = #{uid} and tid = #{tid})")
    public List<Consumer> select(@Param("uid") long uid, @Param("tid") long tid);
    
    /**
     * 根据cluster_id查询consumer
     * @param idList
     * @return List<Topic>
     */
    @Select("select c.*,t.name topicName from consumer c, topic t where c.tid = t.id and t.cluster_id = #{clusterId}")
    public List<Consumer> selectByClusterId(@Param("clusterId") int clusterId);

    /**
     * 根据tid列表批量查询consumer
     * 
     * @param idList
     * @return List<Topic>
     */
    @Select("<script>select * from consumer where tid in "
            + "<foreach collection=\"tidList\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + " order by name</script>")
    public List<Consumer> selectByTidList(@Param("tidList") Collection<Long> tidList);
    
    /**
     * 删除记录
     */
    @Delete("delete from consumer where id = #{id}")
    public Integer delete(@Param("id") long id);
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select c.* from consumer c where c.name = #{name} and c.id in "
            + "(select uc.consumer_id from user_consumer uc where tid = #{tid} and uc.consumer_id = c.id)")
    public Consumer selectTopicConsumerByName(@Param("name") String name, @Param("tid") long tid);
}
