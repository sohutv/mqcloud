package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.NameServer;
/**
 * NameServerDao
 * 
 * @author yongfeigao
 * @date 2018年10月23日
 */
public interface NameServerDao {
    /**
     * 查询
     * 
     * @return
     */
    @Select("select * from name_server where cid = #{cid}")
    public List<NameServer> selectByClusterId(@Param("cid") int cid);

    /**
     * 插入
     * 
     * @param notice
     */
    @Insert("insert into name_server(cid,addr) values(#{cid},#{addr})")
    public Integer insert(@Param("cid") int cid, @Param("addr") String addr);
    
    /**
     * 插入
     * 
     * @param notice
     */
    @Delete("delete from name_server where cid=#{cid} and addr=#{addr}")
    public Integer delete(@Param("cid") int cid, @Param("addr") String addr);
}
