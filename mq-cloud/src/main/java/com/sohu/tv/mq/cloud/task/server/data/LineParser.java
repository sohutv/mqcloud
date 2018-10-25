package com.sohu.tv.mq.cloud.task.server.data;
/**
 * 行解析器
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月18日
 */
public interface LineParser {
	/**
	 * 解析nmon行
	 * @param line     nmon行内容
	 * @param timeKey  时间戳
	 * @throws Exception
	 */
	void parse(String line, String timeKey) throws Exception;
}
