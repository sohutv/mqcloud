package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.ConsumerBlock;
import com.sohu.tv.mq.cloud.bo.ConsumerStat;
import com.sohu.tv.mq.cloud.dao.ConsumerStatDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
/**
 * 消费者监控服务
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月31日
 */
@Service
public class ConsumerMonitorService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConsumerStatDao consumerStatDao;


    /**
     * 查询数量
     */
    public Result<Integer> queryConsumerStatCount() {
        Integer count = null;
        try {
            count = consumerStatDao.selectConsumerStatCount();
        } catch (Exception e) {
            logger.error("selectConsumerStatCount err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 获取消费者监控信息
     * @return
     */
    public List<ConsumerStat> getConsumerStatInfo(int offset, int size) {
        // 获取消费者情况
        List<ConsumerStat> statList = getConsumerStat(offset, size);
        if (statList == null || statList.size() == 0) {
            return null;
        }
        // 获取block情况
        List<ConsumerBlock> blockList = getConsumerBlock(statList);
        if (blockList == null) {
            return statList;
        }
        // 将block组装到stat中
        for (ConsumerBlock block : blockList) {
            for (ConsumerStat stat : statList) {
                if (block.getCsid() == stat.getId()) {
                    List<ConsumerBlock> list = stat.getBlockList();
                    if (list == null) {
                        list = new LinkedList<ConsumerBlock>();
                        stat.setBlockList(list);
                    }
                    list.add(block);
                }
            }
        }
        return statList;
    }

    private List<ConsumerStat> getConsumerStat(int offset, int size) {
        try {
            return consumerStatDao.getConsumerStat(offset, size);
        } catch (Exception e) {
            logger.error("get consumer stat err", e);
        }
        return null;
    }

    private List<ConsumerBlock> getConsumerBlock(List<ConsumerStat> consumerStats) {
        try {
            return consumerStatDao.getConsumerBlock(consumerStats);
        } catch (Exception e) {
            logger.error("get consumer block err", e);
        }
        return null;
    }
}
