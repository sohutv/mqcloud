package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.ShedLock;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 任务执行锁
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/21
 */
public interface ShedLockDao {
    /**
     * 查询
     */
    @Select("select * from shedlock order by locked_at desc")
    public List<ShedLock> select();

    /**
     * 按任务名查询
     */
    @Select("select * from shedlock where name = #{name}")
    public ShedLock selectByName(@Param("name") String name);

    /**
     * 插入
     */
    @Insert("insert into shedlock(name,locked_at,locked_by) values(#{l.name},#{l.lockedAt},#{l.locked_by})")
    public void insert(@Param("l") ShedLock shedLock);

    /**
     * 更新
     */
    @Insert("update shedlock set lock_until = #{tm} where name = #{name}")
    public Integer update(@Param("name") String name, @Param("tm") Date tm);
}
