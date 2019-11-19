package com.sohu.tv.mq.cloud.dao;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserConsumer;

/**
 * 用户消费者dao
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月12日
 */
public interface UserConsumerDao {
    /**
     * 插入记录
     * 
     * @param consumer
     */
    @Insert("insert into user_consumer(uid, tid, consumer_id) values("
            + "#{uc.uid},#{uc.tid},#{uc.consumerId})")
    public void insert(@Param("uc") UserConsumer userConsumer);
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("<script>select * from user_consumer where 1 = 1 "
            + "<if test=\"uc.uid != 0\"> and uid=#{uc.uid} </if>"
            + "<if test=\"uc.tid != 0\"> and tid=#{uc.tid} </if>"
            + "<if test=\"uc.consumerId != 0\"> and consumer_id=#{uc.consumerId} </if>"
            + "</script>")
    public List<UserConsumer> select(@Param("uc") UserConsumer userConsumer);
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("<script>select * from user_consumer where tid = #{tid} and consumer_id in "
            + "<foreach collection=\"cidList\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach>"
            + "</script>")
    public List<UserConsumer> selectByCidList(@Param("tid") long tid, @Param("cidList") Collection<Long> cidList);
    
    /**
     * 删除记录
     */
    @Delete("delete from user_consumer where consumer_id = #{cid}")
    public Integer deleteByConsumerId(@Param("cid") long cid);
    
    /**
     * 删除记录
     */
    @Delete("delete from user_consumer where id = #{id}")
    public Integer deleteById(@Param("id") long id);
    
    /**
     * 查询记录
     * @param consumer
     */
    @Select("select * from user_consumer where tid = #{tid} and consumer_id in "
            + "(select id from consumer where name = #{name})")
    public List<UserConsumer> selectByNameAndTid(@Param("name") String name, @Param("tid") long tid);
    
    /**
     * 查询记录
     * @param consumerID
     */
    @Select("select * from user_consumer where consumer_id = #{consumerID}")
    public List<UserConsumer> selectByConsumerId( @Param("consumerID") long consumerID);
    
    /**
     * 查询记录
     * @param id
     * @return
     */
    @Select("select * from user_consumer where id = #{id}")
    public UserConsumer selectById(@Param("id") long id);
    
    /**
     * 查询用户
     * @param tid
     * @param cid
     * @return
     */
    @Select("select u.* from user_consumer uc, user u where uc.uid = u.id and uc.tid = #{tid} and uc.consumer_id = #{cid}")
    public List<User> selectUserByConsumer(@Param("tid") long tid, @Param("cid") long cid);

    /**
     * 根据uid和consumer查询
     * @param uid
     * @param consumer
     */
    @Select("<script>select distinct tid from user_consumer where 1=1 "
            + "<if test=\"uid != 0\"> and uid = #{uid} </if>"
            +  "and consumer_id in (select id from consumer where name = #{consumer})"
            + "</script>")
    public List<Long> selectTidByUidAndConsumer(@Param("uid")long uid, @Param("consumer")String consumer);
}
