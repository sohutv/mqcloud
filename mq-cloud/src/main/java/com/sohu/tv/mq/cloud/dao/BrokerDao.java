package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.Broker;
/**
 * BrokerDao
 * 
 * @author yongfeigao
 * @date 2018年11月14日
 */
public interface BrokerDao {
    /**
     * 查询
     * 
     * @return
     */
    @Select("select * from broker where cid = #{cid}")
    public List<Broker> selectByClusterId(@Param("cid") int cid);

    /**
     * 插入
     * 
     * @param notice
     */
    @Insert("insert into broker(cid,addr) values(#{cid},#{addr})")
    public Integer insert(@Param("cid") int cid, @Param("addr") String addr);
    
    /**
     * 更新
     * 
     * @param notice
     */
    @Update("update broker set check_status = #{checkStatus}, check_time = now() where cid = #{cid} and addr = #{addr}")
    public Integer update(@Param("cid") int cid, @Param("addr") String addr, @Param("checkStatus") int checkStatus);
    
    /**
     * 删除
     * 
     * @param notice
     */
    @Delete("delete from broker where cid=#{cid}")
    public Integer delete(@Param("cid") int cid);
}
