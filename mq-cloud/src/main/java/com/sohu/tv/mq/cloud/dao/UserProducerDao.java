package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.UserProducer;
import org.apache.ibatis.annotations.*;

import java.util.Collection;
import java.util.List;

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
    @Insert("insert into user_producer(uid, tid, producer, protocol) values("
            + "#{up.uid},#{up.tid},#{up.producer},#{up.protocol})")
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
    
    /**
     * 根据id列表批量查询
     * @param aidList
     * @return List<User>
     */
    @Select("<script>select * from user_producer where id in "
            + "<foreach collection=\"idList\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + "</script>")
    public List<UserProducer> selectByIdList(@Param("idList") Collection<Long> idList);
    
    /**
     * 批量插入记录
     * 
     * @param consumer
     */
    @Insert("<script>insert into user_producer(uid, tid, producer, protocol) values"
            + "<foreach collection=\"upList\" item=\"up\" separator=\",\">"
            + "(#{up.uid},#{up.tid},#{up.producer},#{up.protocol})"
            + "</foreach></script>")
    public Integer batchInsert(@Param("upList") List<UserProducer> userProducerList);

    /**
     * 根据uid查询topicId集合
     * @param uid
     */
    @Select("<script> " +
            "select distinct tid from user_producer where 1=1 " +
            "<if test=\"uid != 0\"> and uid = #{uid} </if> " +
            "</script>")
    List<Long> selectTidListByUid(@Param("uid") long uid);

    /**
     * 根据gid查询topicId集合
     * @param gid
     */
    @Select("<script> " +
            "select distinct tid from user_producer where " +
            " uid in (select id from user where gid = #{gid}) " +
            "</script>")
    List<Long> selectTidListByGid(@Param("gid") long gid);

    /**
     * 校验生产者是否存在
     *
     */
    @Select("<script>select producer from user_producer where producer in "
            + "<foreach collection=\"list\" item=\"name\" separator=\",\" open=\"(\" close=\")\">#{name}</foreach>"
            + "limit 1"
            + "</script>")
    String checkExistByName(@Param("list") List<String> newArrayList);

    /**
     * 依据生产者名称获取关联用户ID
     *
     */
    @Select("select distinct uid from user_producer where producer = #{name}")
    List<Integer> selectUidByProduceName(@Param("name") String groupClientName);
}
