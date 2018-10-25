package com.sohu.tv.mq.cloud.bo;

import java.util.Date;
import java.util.List;
/**
 * 消费状况
 * @author yongfeigao
 *
 */
public class ConsumerStat {
	//id
	private int id;
	//消费组
	private String consumerGroup;
	//消费主题
	private String topic;
	//未处理消息总数
	private String undoneMsgCount;
	//单队列最大未处理消息数
	private String undone1qMsgCount;
	//未处理消息时延(broker最新消息存储时间-最新消费时间),ms
	private int undoneDelay;
	//错误的订阅关系
	private String sbscription;
	//记录更新时间
	private Date updatetime;
	//阻塞情况
	private List<ConsumerBlock> blockList;
	private long tid;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getConsumerGroup() {
		return consumerGroup;
	}
	public void setConsumerGroup(String consumerGroup) {
		this.consumerGroup = consumerGroup;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getUndoneMsgCount() {
		return undoneMsgCount;
	}
	public void setUndoneMsgCount(String undoneMsgCount) {
		this.undoneMsgCount = undoneMsgCount;
	}
	public String getUndone1qMsgCount() {
		return undone1qMsgCount;
	}
	public void setUndone1qMsgCount(String undone1qMsgCount) {
		this.undone1qMsgCount = undone1qMsgCount;
	}
	public int getUndoneDelay() {
		return undoneDelay;
	}
	public void setUndoneDelay(int undoneDelay) {
		this.undoneDelay = undoneDelay;
	}
	public String getSbscription() {
		return sbscription;
	}
	public void setSbscription(String sbscription) {
		this.sbscription = sbscription;
	}
	public Date getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}
	public List<ConsumerBlock> getBlockList() {
		return blockList;
	}
	public void setBlockList(List<ConsumerBlock> blockList) {
		this.blockList = blockList;
	}
	public long getTid() {
        return tid;
    }
    public void setTid(long tid) {
        this.tid = tid;
    }
    @Override
	public String toString() {
		return "ConsumerStat [id=" + id + ", consumerGroup=" + consumerGroup
				+ ", topic=" + topic + ", undoneMsgCount=" + undoneMsgCount
				+ ", undone1qMsgCount=" + undone1qMsgCount + ", undoneDelay="
				+ undoneDelay + ", sbscription=" + sbscription
				+ ", updatetime=" + updatetime + "]";
	}
}
