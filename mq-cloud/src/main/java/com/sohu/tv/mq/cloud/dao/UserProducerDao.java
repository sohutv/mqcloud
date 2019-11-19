package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.UserProducer;

/**
 * 用户生产者dao
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月12日
 */
public interface UserProducerDao {
    /**
     * 插入记录
     * 
     * @param consumer
     */
    @Options(useGeneratedKeys = true, keyProperty = "up.id")
    @Insert("insert into user_producer(uid, tid, producer) values("
            + "#{up.uid},#{up.tid},#{up.producer})")
    public Integer insert(@Param("up") UserProducer userProducer);
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from user_producer where tid = #{tid}")
    public List<UserProducer> selectByTid(@Param("tid") long tid);
    
    /**
     * 删除记录
     * @param consumer
     */
    @Delete("delete from user_producer where tid = #{tid}")
    public Integer deleteByTid(@Param("tid") long tid);
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from user_producer where uid = #{uid}")
    public List<UserProducer> selectByUid(@Param("uid") long uid);
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from user_producer where tid = #{tid} and producer = #{producer} limit 1")
    public UserProducer selectByName(@Param("producer") String producer, @Param("tid") long tid);
    
    /**
     * 删除用户在user_producer表中的对应关系
     * @param id
     * @return
     */
    @Delete("delete from user_producer where id = #{id}")
    public Integer deleteByID(@Param("id") long id);

    /**
     * 查询记录
     * @param pid
     */
    @Select("select * from user_producer where id = #{pid}")
    public UserProducer selectByPid(@Param("pid") long pid);
    
    /**
     * 查询记录
     * @param producer
     */
    @Select("select * from user_producer where producer = #{producer}")
    public List<UserProducer> selectByProducer(@Param("producer") String producer);
    
    /**
     * 查询记录
     * @param producer
     */
    @Select("select * from user_producer where producer = #{producer} and uid = #{uid}")
    public List<UserProducer> selectByProducerAndUid(@Param("producer") String producer, @Param("uid") long uid);
    
    /**
     * 查询记录
     * @param tid
     * @param uid
     */
    @Select("select * from user_producer where tid = #{tid} and uid = #{uid} limit 1")
    public UserProducer selectByTidAndUid(@Param("uid") long uid, @Param("tid") long tid);

    /**
     * 根据uid和producer查询topicId
     * @param uid
     * @param producer
     */
    @Select("<script> " +
            "select distinct tid from user_producer where 1=1 " +
            "<if test=\"uid != 0\"> and uid = #{uid} </if> " +
            "and producer = #{producer} " +
            "</script>")
    public List<Long> selectTidByProducerAndUid(@Param("uid") long uid, @Param("producer") String producer);
}
