package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.ServerInfo;
import com.sohu.tv.mq.cloud.bo.ServerInfoExt;
import com.sohu.tv.mq.cloud.bo.ServerStatus;
import com.sohu.tv.mq.cloud.task.server.data.Server;

/**
 * 服务器状态信息持久化
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月18日
 */
public interface ServerStatusDao {
    
    /**
     * 查询服务器基本信息
     * @param ip
     * @return @ServerInfo
     */
    @Select("select * from server")
    public List<ServerInfo> queryAllServerInfo();
    
    /**
     * 查询服务器目前的状况
     * @param ip
     * @return @ServerInfo
     */
    @Select("select * from server s left join server_stat ss on ss.ip = s.ip and ss.cdate=#{cdate} and ss.ctime in "
            + "(select max(ctime) from server_stat where cdate=#{cdate})")
    public List<ServerInfoExt> queryAllServer(@Param("cdate") String date);

    /**
     * 查询服务器基本信息
     * @param ip
     * @return @ServerInfo
     */
    @Select("select * from server where ip=#{ip}")
    public ServerInfo queryServerInfo(@Param("ip") String ip);
    
    /**
     * 保存服务器发行版信息
     * @param ip
     * @param dist from /etc/issue
     */
    @Insert("<script>insert ignore into server(ip,dist<if test=\"type >= 0\">,machine_type</if>) "
            + "values (#{ip},#{dist}<if test=\"type >= 0\">,#{type}</if>)</script>")
    public void saveServerInfo(@Param("ip") String ip, @Param("dist") String dist, @Param("type") int type);
    
    /**
     * 删除服务器信息
     * @param ip
     * @return 删除的数量
     */
    @Update("delete from server where ip=#{ip}")
    public Integer deleteServerInfo(@Param("ip") String ip);
    
    /**
     * 保存/更新服务器信息
     * @param server
     * @return 影响的行数
     */
    @Update("insert into server (ip,host,nmon,cpus,cpu_model,kernel,ulimit) values "
            + "(#{server.ip},#{server.host},#{server.nmon},#{server.cpus},#{server.cpuModel},#{server.kernel},#{server.ulimit}) "
            + "on duplicate key update host=values(host), nmon=values(nmon), cpus=values(cpus), "
            + "cpu_model=values(cpu_model), kernel=values(kernel), ulimit=values(ulimit)")
    public Integer saveAndUpdateServerInfo(@Param("server")Server server);
	
	/**
	 * 查询服务器状态
	 * @param ip
	 * @param date
	 * @return List<ServerStatus>
	 */
    @Select("select * from server_stat where ip=#{ip} and cdate=#{cdate}")
	public List<ServerStatus> queryServerStat(@Param("ip") String ip, 
			@Param("cdate") String date);
	

	/**
	 * 保存服务器状态
	 * @param ServerInfoExt
	 */
	@Insert("insert ignore into server_stat(ip,cdate,ctime,cuser,csys,cwio,c_ext," + 
	        "cload1,cload5,cload15," + 
	        "mtotal,mfree,mcache,mbuffer,mswap,mswap_free," + 
	        "nin,nout,nin_ext,nout_ext," + 
	        "tuse,torphan,twait," + 
	        "dread,dwrite,diops,dbusy,d_ext,dspace)" + 
	        "values(#{server.ip},#{server.collectTime},#{server.time}," + 
	        "#{server.cpu.user},#{server.cpu.sys},#{server.cpu.wait},#{server.cpu.ext}," + 
	        "#{server.load.load1},#{server.load.load5},#{server.load.load15}," + 
	        "#{server.mem.total},#{server.mem.totalFree},#{server.mem.cache}," + 
	        "#{server.mem.buffer},#{server.mem.swap},#{server.mem.swapFree}," + 
	        "#{server.net.nin},#{server.net.nout},#{server.net.ninDetail},#{server.net.noutDetail}," + 
	        "#{server.connection.established},#{server.connection.orphan},#{server.connection.timeWait}," + 
	        "#{server.disk.read},#{server.disk.write},#{server.disk.iops},#{server.disk.busy}," + 
	        "#{server.disk.ext},#{server.disk.space})")
	public void saveServerStat(@Param("server") Server server);
	
	/**
	 * 删除数据
	 * @param date
	 * @return
	 */
	@Delete("delete from server_stat where cdate < #{cdate}")
	public Integer deleteServerStat(@Param("cdate") String date);
	
	/**
     * 删除数据
     * @param ip
     * @return
     */
    @Delete("delete from server where ip = #{ip}")
    public Integer deleteServer(@Param("ip") String ip);
    
    /**
     * 修改数据
     * @param ip
     * @param type
     * @return
     */
    @Update("update server set machine_type=#{type} where ip = #{ip}")
    public Integer updateServer(@Param("ip") String ip, @Param("type") int type);
}
