package com.sohu.tv.mq.cloud.dao;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.sohu.tv.mq.cloud.bo.ServerAlarmConfig;

/**
 * 服务器预警配置
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年10月31日
 */
public interface ServerAlarmConfigDao {

    /**
     * 查询记录
     */
    @Select("select * from server_warn_config where ip=#{ip}")
    public ServerAlarmConfig selectByIp(@Param("ip") String ip);

    /**
     * 查询所有记录
     */
    @Select("select * from server_warn_config")
    public List<ServerAlarmConfig> selectAll();

    /**
     * 修改记录
     */
    @Insert("<script>replace into server_warn_config (ip, memory_usage_rate, load1, connect"
            + ", wait,iops, iobusy, cpu_usage_rate, net_in, net_out, io_usage_rate) values "
            + "<foreach collection=\"ipList\" item=\"ip\" separator=\",\">(#{ip}"
            + ", #{sac.memoryUsageRate}, #{sac.load1}, #{sac.connect}, #{sac.wait}, #{sac.iops}"
            + ", #{sac.iobusy}, #{sac.cpuUsageRate}, #{sac.netIn}, #{sac.netOut}"
            + ", #{sac.ioUsageRate})</foreach> "
            + "</script>")
    public Integer update(@Param("sac") ServerAlarmConfig serverAlarmConfig,
            @Param("ipList") Collection<String> ipList);
    
    /**
     * 删除记录
     */
    @Delete("delete from server_warn_config where ip=#{ip}")
    public Integer delete(@Param("ip") String ip);
}
