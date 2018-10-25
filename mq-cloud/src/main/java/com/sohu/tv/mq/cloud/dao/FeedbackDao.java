package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sohu.tv.mq.cloud.bo.Feedback;

/**
 * 反馈dao
 * 
 * @author yongfeigao
 * @date 2018年9月18日
 */
public interface FeedbackDao {
    /**
     * 查询
     * 
     * @return
     */
    @Select("select * from feedback order by id desc")
    public List<Feedback> selectAll();

    /**
     * 插入
     * 
     * @param notice
     */
    @Insert("insert into feedback(uid,content,create_date) values(#{feedback.uid},#{feedback.content},now())")
    public void insert(@Param("feedback") Feedback feedback);
}
