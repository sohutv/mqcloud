package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.MessageExport;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * 消息导出
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/25
 */
public interface MessageExportDao {

    /**
     * 按aid查询
     */
    @Select("select * from message_export where aid=#{aid}")
    public MessageExport select(@Param("aid") long aid);

    /**
     * 插入
     */
    @Insert("insert into message_export(aid,ip,info) values(#{me.aid},#{me.ip},#{me.info})")
    public void insert(@Param("me") MessageExport messageExport);

    /**
     * 更新
     */
    @Update("<script>update message_export set aid=#{me.aid} "
            + "<if test=\"me.totalMsgCount != 0\">,total_msg_count=#{me.totalMsgCount}</if>"
            + "<if test=\"me.exportedMsgCount != 0\">,exported_msg_count=#{me.exportedMsgCount}</if>"
            + "<if test=\"me.leftTime != 0\">,left_time=#{me.leftTime}</if>"
            + "<if test=\"me.exportCostTime != 0\">,export_cost_time=#{me.exportCostTime}</if>"
            + "<if test=\"me.compressCostTime != 0\">,compress_cost_time=#{me.compressCostTime}</if>"
            + "<if test=\"me.scpCostTime != 0\">,scp_cost_time=#{me.scpCostTime}</if>"
            + "<if test=\"me.exportedFilePath != null\">,exported_file_path=#{me.exportedFilePath}</if>"
            + "<if test=\"me.info != null\">,info=#{me.info}</if>"
            + " where aid = #{me.aid}" +
            "</script>")
    public Integer update(@Param("me") MessageExport messageExport);

    /**
     * 删除
     *
     * @return
     */
    @Delete("delete from message_export where aid=#{aid}")
    public Integer delete(@Param("aid") long aid);

    /**
     * 查询导出任务执行超过time未更新的
     */
    @Select("select m.* from message_export m, audit a where a.status = 4 and a.id = m.aid and m.update_time <= #{tm}")
    public List<MessageExport> selectLaterThan(@Param("tm") Date time);
}
