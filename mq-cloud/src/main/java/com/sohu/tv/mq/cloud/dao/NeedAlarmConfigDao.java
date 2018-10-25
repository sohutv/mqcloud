package com.sohu.tv.mq.cloud.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.NeedAlarmConfig;

/**
 * 预警频率控制
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月8日
 */
public interface NeedAlarmConfigDao {

    /**
     * 查询
     */
    @Select("select * from need_warn_config where oKey = #{oKey}")
    public NeedAlarmConfig get(@Param("oKey") String oKey);

    /**
     * 插入记录
     */
    @Insert("insert into need_warn_config(oKey,times,update_time) values(#{nac.oKey},#{nac.times},#{nac.updateTime})")
    public Integer insert(@Param("nac") NeedAlarmConfig needAlarmConfig);

    /**
     * 重置
     */
    @Update("update need_warn_config set times = 0,update_time = #{updateTime} where oKey = #{oKey}")
    public Integer reset(@Param("oKey") String oKey, @Param("updateTime") long updateTime);

    /**
     * 计数
     */
    @Update("update need_warn_config set times = times+1 where oKey = #{oKey}")
    public Integer updateTimes(@Param("oKey") String oKey);
}
