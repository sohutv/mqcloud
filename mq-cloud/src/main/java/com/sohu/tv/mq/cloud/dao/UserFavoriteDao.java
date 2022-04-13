package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.UserFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户收藏
 * @author: yongfeigao
 * @date: 2022/3/21 15:33
 */
public interface UserFavoriteDao {

    /**
     * 根据uid查询记录量
     *
     * @param uid
     * @return
     */
    @Select("select count(1) from user_favorite where uid = #{uid}")
    public Integer selectCount(@Param("uid") long uid);

    /**
     * 查询
     *
     * @return
     */
    @Select("select * from user_favorite where uid = #{uid} order by create_time desc limit #{m},#{n}")
    public List<UserFavorite> selectByPage(@Param("uid") long uid, @Param("m") int offset, @Param("n") int size);

    /**
     * 查询
     *
     * @return
     */
    @Select("select * from user_favorite where uid = #{uid} and tid = #{tid}")
    public UserFavorite select(@Param("uid") long uid, @Param("tid") long tid);

    /**
     * 查询
     *
     * @return
     */
    @Select("select * from user_favorite where id = #{id}")
    public UserFavorite selectById(@Param("id") long id);

    /**
     * 插入
     *
     * @param notice
     */
    @Insert("insert ignore into user_favorite(uid, tid) values(#{uf.uid},#{uf.tid})")
    public Integer insert(@Param("uf") UserFavorite userFavorite);

    /**
     * 删除
     *
     * @param tid
     */
    @Delete("delete from user_favorite where id=#{id}")
    public Integer deleteById(@Param("id") long id);

    /**
     * 删除
     *
     * @param tid
     */
    @Delete("delete from user_favorite where tid=#{tid}")
    public Integer deleteByTid(@Param("tid") long tid);
}
