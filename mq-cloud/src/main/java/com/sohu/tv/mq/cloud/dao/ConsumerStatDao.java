package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.apache.ibatis.annotations.*;

import com.sohu.tv.mq.cloud.bo.ConsumerBlock;
import com.sohu.tv.mq.cloud.bo.ConsumerStat;
/**
 * 消费者状态
 * @author yongfeigao
 *
 */
public interface ConsumerStatDao {
	/**
	 * 保存消费状态
	 * @param consumerGroup
	 * @param topic
	 * @param undoneMsgCount
	 * @param undone1qMsgCount
	 * @param undoneDelay
	 * @return
	 */
	@Insert("insert into consumer_stat(consumer_group,topic,undone_msg_count,undone_1q_msg_count,undone_delay) " +
	"values(#{consumerGroup},#{topic},#{undoneMsgCount},#{undone1qMsgCount},#{undoneDelay})" +
	"on duplicate key update updatetime=CURRENT_TIMESTAMP, undone_msg_count=values(undone_msg_count)," +
	"undone_1q_msg_count=values(undone_1q_msg_count)," +
	"undone_delay=values(undone_delay)")
	public Integer saveConsumerStat(@Param("consumerGroup") String consumerGroup, 
			@Param("topic")String topic,
			@Param("undoneMsgCount") int undoneMsgCount, 
			@Param("undone1qMsgCount")int undone1qMsgCount,
			@Param("undoneDelay") long undoneDelay);
	
	/**
     * 保存简单的消费状态
     * @param consumerGroup
     * @param topic
     * @return
     */
	@Options(useGeneratedKeys = true, keyProperty = "cs.id")
    @Insert("<script>insert into consumer_stat(consumer_group"
            + "<if test=\"cs.topic != null\">,topic</if> "
            + "<if test=\"cs.sbscription != null\">,sbscription</if> "
            + ") values(#{cs.consumerGroup}"
            + "<if test=\"cs.topic != null\">,#{cs.topic}</if> "
            + "<if test=\"cs.sbscription != null\">,#{cs.sbscription}</if> "
            + ")" +
            " on duplicate key update updatetime=CURRENT_TIMESTAMP"
            + "<if test=\"cs.topic != null\">,topic=values(topic)</if> "
            + "<if test=\"cs.sbscription != null\">,sbscription=values(sbscription)</if> "
            + "</script>")
    public Integer saveSimpleConsumerStat(@Param("cs") ConsumerStat consumerStat);
	
	/**
	 * 保存阻塞状态
	 * @param csid
	 * @param instance
	 * @param broker
	 * @param qid
	 * @param blockTime
	 * @return
	 */
	@Insert("insert into consumer_block(csid,instance,broker,qid,block_time) " +
	"values(#{csid},#{instance},#{broker},#{qid},#{blockTime})" +
	"on duplicate key update instance=values(instance)," +
	"block_time=values(block_time)")
	public Integer saveConsumerBlock(@Param("csid")int csid,
			@Param("instance") String instance, 
			@Param("broker")String broker,
			@Param("qid") int qid, 
			@Param("blockTime")long blockTime);
	
	/**
	 * 保存阻塞状态
	 * @param csid
	 * @param broker
	 * @param qid
	 * @param offsetMovedTime
	 * @return
	 */
	@Insert("insert into consumer_block(csid,broker,qid,offset_moved_time,offset_moved_times) " +
	"values(#{csid},#{broker},#{qid},#{offsetMovedTime},1) " +
	"on duplicate key update offset_moved_time=values(offset_moved_time),offset_moved_times=offset_moved_times+1")
	public Integer saveSomeConsumerBlock(@Param("csid") int csid,
			@Param("broker") String broker,
			@Param("qid") int qid, 
			@Param("offsetMovedTime")long offsetMovedTime);
	
	@Select("select id,consumer_group consumerGroup,topic,undone_msg_count undoneMsgCount," +
			"undone_1q_msg_count undone1qMsgCount," +
			"undone_delay undoneDelay,sbscription,updatetime" +
			" FROM consumer_stat ORDER BY updatetime desc")
	public List<ConsumerStat> getConsumerStat();
	
	@Select("select csid,instance,broker,qid,block_time blockTime,updatetime," +
			"offset_moved_time offsetMovedTime, offset_moved_times offsetMovedTimes " +
			"FROM consumer_block " +
			"ORDER BY updatetime desc")
	public List<ConsumerBlock> getConsumerBlock();
}
