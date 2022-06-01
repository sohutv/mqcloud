package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.bo.ClientLanguage;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.cursor.Cursor;

import java.util.List;

/**
 * clientLanguaage dao
 * 
 * @Description: 客户端语言
 * @author wf
 * @date 2018年6月12日
 */
public interface ClientLanguageDao {

    /** 插入or更新
     * @param clientLanguage 客户端语言版本
     * @return
     */
    @Insert("insert into client_language(cid,tid,client_group_name, client_group_type, language, version, relation_uids, create_date, update_time) "
            + "values(#{clientLanguage.cid},#{clientLanguage.tid},#{clientLanguage.clientGroupName},#{clientLanguage.clientGroupType},#{clientLanguage.language},"
            + "#{clientLanguage.version},#{clientLanguage.relationUids},#{clientLanguage.createDate},#{clientLanguage.updateTime}) "
            + "on duplicate key update update_time=CURRENT_TIMESTAMP")
    public Integer insert(@Param("clientLanguage") ClientLanguage clientLanguage);


    /** 手动更新
     * @param clientLanguage 客户端语言版本
     * @return
     */
    @Update("<script> " +
            "update client_language set update_time = now(),modify_type = 1 " +
            "<if test=\"clientLanguage.language != null \">,language = #{clientLanguage.language}</if> " +
            "<if test=\"clientLanguage.version != null and clientLanguage.version != ''\">, version = #{clientLanguage.version}</if> " +
            "where tid = #{clientLanguage.tid} and client_group_name = #{clientLanguage.clientGroupName} " +
            "</script>")
    public Integer update(@Param("clientLanguage") ClientLanguage clientLanguage);


    /** 条件单表查询
     * @param clientLanguage 客户端语言版本
     * @return
     */
    @Select("<script>" +
            "select * from client_language where 1=1" +
            "<if test=\"clientLanguage.cid != null \">and cid = #{clientLanguage.cid} </if> " +
            "<if test=\"clientLanguage.tid != null \">and tid = #{clientLanguage.tid} </if> " +
            "<if test=\"clientLanguage.clientGroupName != null and clientLanguage.clientGroupName != ''\">and client_group_name = #{clientLanguage.clientGroupName}</if> " +
            "<if test=\"clientLanguage.clientGroupType != null \">and client_group_type = #{clientLanguage.clientGroupType}</if> " +
            "<if test=\"clientLanguage.language != null \">and language = #{clientLanguage.language}</if> " +
            "<if test=\"clientLanguage.version != null and clientLanguage.version != ''\">and version = #{clientLanguage.version}</if> " +
            "<if test=\"offset != null and pageSize != null\">LIMIT #{offset},#{pageSize}</if> " +
            "</script>")
    public List<ClientLanguage> selectByParams(@Param("clientLanguage") ClientLanguage clientLanguage,@Param("offset") Integer offset,
                                               @Param("pageSize") Integer pageSize);

    /** 条件单表查询总数
     * @param clientLanguage 客户端语言版本
     * @return
     */
    @Select("<script>" +
            "select count(1) from client_language where 1=1" +
            "<if test=\"clientLanguage.cid != null \">and cid = #{clientLanguage.cid} </if> " +
            "<if test=\"clientLanguage.tid != null \">and tid = #{clientLanguage.tid} </if> " +
            "<if test=\"clientLanguage.clientGroupName != null and clientLanguage.clientGroupName != ''\">and client_group_name = #{clientLanguage.clientGroupName}</if> " +
            "<if test=\"clientLanguage.clientGroupType != null \">and client_group_type = #{clientLanguage.clientGroupType}</if> " +
            "<if test=\"clientLanguage.language != null \">and language = #{clientLanguage.language}</if> " +
            "<if test=\"clientLanguage.version != null and clientLanguage.version != ''\">and version = #{clientLanguage.version}</if> " +
            "</script>")
    public Integer selectCountByParams(@Param("clientLanguage") ClientLanguage clientLanguage);


    /** 查询tid
     * @param List<Long> tid集合
     * @return
     */
    @Select("<script> " +
            "select DISTINCT(tid) from client_language " +
            "<if test=\"language != null \">where language = #{language} </if> " +
            "</script>")
    public List<Long> selectTidByParams(@Param("language")byte language);


    /** 依据tid范围查询
     * @param clientLanguage 客户端语言版本
     * @return
     */
    @Select("<script>select * from client_language where tid in "
            + "<foreach collection=\"tids\" item=\"id\" separator=\",\" open=\"(\" close=\")\">#{id}</foreach> "
            + "</script>")
    public List<ClientLanguage> selectRangeDataByTids(@Param("tids") List<Long> tids);


    /** 查询所有的客户端语言
     * @param List<Byte>
     * @return
     */
    @Select("select DISTINCT(language) from client_language")
    List<Byte> selectAllLanguage();

    /** 查询所有的客户端语言
     * @param List<Byte>
     * @return
     */
    @Select("select DISTINCT(client_group_name) from client_language")
    List<String> selectgetAllGroupName();
}
