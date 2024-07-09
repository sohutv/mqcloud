package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.Controller;
import com.sohu.tv.mq.cloud.bo.NameServer;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ControllerDao
 *
 * @author yongfeigao
 * @date 2023年05月22日
 */
public interface ControllerDao {
    /**
     * 查询
     *
     * @return
     */
    @Select("select * from controller where cid = #{cid}")
    public List<Controller> selectByClusterId(@Param("cid") int cid);

    /**
     * 查询全部
     *
     * @return
     */
    @Select("select * from controller")
    public List<Controller> selectAll();

    /**
     * 插入
     *
     * @param notice
     */
    @Insert("<script>insert into controller(cid,addr"
            + "<if test=\"baseDir != null\">,base_dir</if> "
            + ") values(#{cid},#{addr}"
            + "<if test=\"baseDir != null\">,#{baseDir}</if> "
            + ") <if test=\"baseDir != null\">on duplicate key update base_dir=values(base_dir)</if>" +
            "</script>")
    public Integer insert(@Param("cid") int cid, @Param("addr") String addr, @Param("baseDir") String baseDir);

    /**
     * 更新
     *
     * @param notice
     */
    @Update("update controller set check_status = #{checkStatus}, check_time = now()  where cid = #{cid} and addr = #{addr}")
    public Integer update(@Param("cid") int cid, @Param("addr") String addr, @Param("checkStatus") int checkStatus);

    /**
     * 删除
     *
     * @param notice
     */
    @Delete("delete from controller where cid=#{cid} and addr=#{addr}")
    public Integer delete(@Param("cid") int cid, @Param("addr") String addr);
}
