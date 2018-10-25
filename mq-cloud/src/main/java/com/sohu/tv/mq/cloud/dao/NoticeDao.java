package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sohu.tv.mq.cloud.bo.Notice;

/**
 * 通知
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
public interface NoticeDao {
    
    /**
     * 查询当前的通知
     * @return
     */
    @Select("select * from notice where status = 1 order by id limit 1")
    public Notice selectNow();
    
    /**
     * 查询
     * @return
     */
    @Select("select * from notice")
    public List<Notice> select();
    
    /**
     * 插入
     * @param notice
     */
    @Insert("insert into notice(content,status,create_date) values(#{notice.content},#{notice.status},now())")
    public void insert(@Param("notice")Notice notice);
    
    /**
     * 更新
     * @param notice
     * @return
     */
    @Update("update notice set content=#{notice.content}, status=#{notice.status} where id=#{notice.id}")
    public Integer update(@Param("notice")Notice notice);
    
    /**
     * 删除
     * @return
     */
    @Delete("delete from notice where id=#{id}")
    public Integer delete(@Param("id")long id);
}
