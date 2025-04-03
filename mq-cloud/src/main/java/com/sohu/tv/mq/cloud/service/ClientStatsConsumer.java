package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.ProducerStat;
import com.sohu.tv.mq.cloud.bo.ProducerTotalStat;
import com.sohu.tv.mq.cloud.common.MemoryMQConsumer;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.stats.InvokeStats.InvokeStatsResult;
import com.sohu.tv.mq.stats.dto.ClientStats;
import com.sohu.tv.mq.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * 客户端统计消费
 * 
 * @author yongfeigao
 * @date 2018年9月12日
 */
@Component
public class ClientStatsConsumer implements MemoryMQConsumer<ClientStats> {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ProducerTotalStatService producerTotalStatService;
    
    @Autowired
    private ProducerStatService producerStatService;

    @Override
    public void consume(ClientStats clientStats) throws Exception {
        ProducerTotalStat producerTotalStat = generateProducerTotalStat(clientStats);
        Result<Integer> result = producerTotalStatService.save(producerTotalStat);
        long id = producerTotalStat.getId();
        if(id == 0) {
            // 有异常
            if(result.getException() != null) {
                if(!(result.getException() instanceof DuplicateKeyException)
                        && !(result.getException() instanceof DataIntegrityViolationException)) {
                    // 数据库错误，可以进行重试
                    throw result.getException();
                } else {
                    // 数据重复，重试一次
                    if(clientStats.getClient().indexOf("@") == -1) {
                        clientStats.setClient(clientStats.getClient() + "@1");
                        consume(clientStats);
                        return;
                    }
                }
            }
            logger.error("save producerTotalStat:{} err", producerTotalStat);
            return;
        }
        // 生成ProducerStat
        List<ProducerStat> list = generateProducerStat(id, clientStats);
        if(list != null && list.size() > 0) {
            result = producerStatService.save(list);
            if(result.isNotOK()) {
                if(result.getException() != null) {
                    logger.error("save producerStat:{} err", list, result.getException());
                } else {
                    logger.error("save producerStat:{} err", list);
                }
            }
        }
    }
    
    /**
     * 生成ProducerTotalStat
     * @param clientStats
     * @return
     */
    private ProducerTotalStat generateProducerTotalStat(ClientStats clientStats) {
        // 对象拼装
        ProducerTotalStat producerTotalStat = new ProducerTotalStat();
        producerTotalStat.setAvg(clientStats.getAvg());
        producerTotalStat.setClient(clientStats.getClient());
        producerTotalStat.setIp(producerTotalStat.getClient().split("@")[0]);
        producerTotalStat.setCount(clientStats.getCounts());
        producerTotalStat.setPercent90(clientStats.getPercent90());
        producerTotalStat.setPercent99(clientStats.getPercent99());
        producerTotalStat.setProducer(clientStats.getProducer());
        producerTotalStat.setStatTime(clientStats.getStatsTime());
        Date now = new Date();
        producerTotalStat.setCreateDate(DateUtil.format(now));
        producerTotalStat.setCreateTime(DateUtil.getFormat(DateUtil.HHMM).format(now));
        if(clientStats.getExceptionMap() != null && clientStats.getExceptionMap().size() > 0) {
            producerTotalStat.setException(JSONUtil.toJSONString(clientStats.getExceptionMap()));
        }
        return producerTotalStat;
    }
    
    /**
     * 生成ProducerStat
     * @param clientStats
     * @return
     */
    private List<ProducerStat> generateProducerStat(long id, ClientStats clientStats) {
        Map<String, InvokeStatsResult> map = clientStats.getDetailInvoke();
        if(map == null) {
            return null;
        }
        List<ProducerStat> producerStatList = new ArrayList<ProducerStat>();
        for(Entry<String, InvokeStatsResult> entry : map.entrySet()) {
            ProducerStat producerStat = new ProducerStat();
            producerStat.setBroker(entry.getKey());
            producerStat.setAvg(entry.getValue().getAvgTime());
            producerStat.setCount(entry.getValue().getTimes());
            producerStat.setMax(entry.getValue().getMaxTime());
            producerStat.setTotalId(id);
            // 处理异常记录
            Map<String, Integer> exceptionMap = entry.getValue().getExceptionMap();
            if(exceptionMap != null) {
                producerStat.setException(JSONUtil.toJSONString(exceptionMap));
            }
            producerStatList.add(producerStat);
        }
        return producerStatList;
    }
}
