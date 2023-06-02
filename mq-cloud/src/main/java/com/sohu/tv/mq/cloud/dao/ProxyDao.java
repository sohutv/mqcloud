package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.Proxy;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ProxyDao
 *
 * @author yongfeigao
 * @date 2023年05月25日
 */
public interface ProxyDao {
    /**
     * 查询
     *
     * @return
     */
    @Select("select * from proxy where cid = #{cid}")
    public List<Proxy> selectByClusterId(@Param("cid") int cid);

    /**
     * 查询全部
     *
     * @return
     */
    @Select("select * from proxy")
    public List<Proxy> selectAll();

    /**
     * 插入
     *
     * @param notice
     */
    @Insert("<script>insert into proxy(cid,addr"
            + "<if test=\"px.baseDir != null\">,base_dir</if> "
            + "<if test=\"px.config != null\">,config</if> "
            + ") values(#{px.cid},#{px.addr}"
            + "<if test=\"px.baseDir != null\">,#{px.baseDir}</if> "
            + "<if test=\"px.config != null\">,#{px.config}</if> "
            + ")</script>")
    public Integer insert(@Param("px") Proxy proxy);

    /**
     * 更新
     *
     * @param notice
     */
    @Update("update proxy set check_status = #{checkStatus}, check_time = now()  where cid = #{cid} and addr = #{addr}")
    public Integer update(@Param("cid") int cid, @Param("addr") String addr, @Param("checkStatus") int checkStatus);

    /**
     * 删除
     *
     * @param notice
     */
    @Delete("delete from proxy where cid=#{cid} and addr=#{addr}")
    public Integer delete(@Param("cid") int cid, @Param("addr") String addr);
}
