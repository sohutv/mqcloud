package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.CancelUniqId;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description 取消定时消息ID记录表Dao
 * @date 2023/7/28 16:38:51
 */
public interface CancelUniqIdDao {

    /**
     * 保存单条记录
     */
    @Insert("insert into cancel_uniqid(tid, uniqueId, createTime) values(#{tid}, #{uniqId}, #{createTime})")
    int save(@Param("tid") Long tid, @Param("uniqId") String uniqId, @Param("createTime")Date createTime);

    /**
     * 按照uniqueIds批量查询
     */
    @Select("<script>" +
            "select * from cancel_uniqid where tid = #{tid} and uniqueId in " +
            "<foreach collection=\"uniqIds\" item=\"s\" open=\"(\" separator=\",\" close=\")\">" +
            "#{s}" +
            "</foreach>" +
            "</script>")
    List<CancelUniqId> queryByUniqIds(@Param("tid") Long tid, @Param("uniqIds") List<String> uniqIds);

    /**
     * 按照uniqueId查询
     */
    @Select("select * from cancel_uniqid where uniqueId = #{uniqId}")
    CancelUniqId queryOneByUniqId(String uniqId);
}
