package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.UserFootprint;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户足迹
 *
 * @author: yongfeigao
 * @date: 2022/3/9 15:28
 */
public interface UserFootprintDao {

    /**
     * 根据uid查询记录量
     *
     * @param uid
     * @return
     */
    @Select("select count(1) from user_footprint where uid = #{uid}")
    public Integer selectCount(@Param("uid") long uid);

    /**
     * 查询
     *
     * @return
     */
    @Select("select * from user_footprint where uid = #{uid} order by update_time desc limit #{m},#{n}")
    public List<UserFootprint> selectByPage(@Param("uid") long uid, @Param("m") int offset, @Param("n") int size);

    /**
     * 插入
     *
     * @param notice
     */
    @Insert("insert into user_footprint(uid, tid) values(#{fp.uid},#{fp.tid}) on duplicate key update " +
            "update_time=CURRENT_TIMESTAMP")
    public Integer insert(@Param("fp") UserFootprint userFootprint);

    /**
     * 删除
     *
     * @param tid
     */
    @Delete("delete from user_footprint where tid=#{tid}")
    public Integer deleteByTid(@Param("tid") long tid);
}
