package com.sohu.tv.mq.cloud.web.controller;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.common.MemoryMQ;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.WebUtil;
import com.sohu.tv.mq.cloud.web.controller.param.TopicUserParam;
import com.sohu.tv.mq.dto.ClusterInfoDTO;
import com.sohu.tv.mq.stats.dto.ClientStats;
import com.sohu.tv.mq.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
/**
 * 集群信息查询
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月3日
 */
@RestController
@RequestMapping("/cluster")
public class ClusterController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private TopicService topicService;
    
    @Autowired
    private ConsumerService consumerService;
    
    @Autowired
    private UserProducerService userProducerService;
    
    @Autowired
    private ClientVersionService clientVersionService;
    
    @Autowired
    private MemoryMQ<ClientStats> clientStatsMemoryMQ;
    
    @Autowired
    private ClusterService clusterService;
    
    /**
     * 查询topic的cluster，并校验所属关系
     * @param topicParam
     * @return
     * @throws Exception
     */
    @RequestMapping("/info")
    public Result<?> info(@Valid TopicUserParam topicUserParam, HttpServletRequest request) throws Exception {
        String ip = WebUtil.getIp(request);
        // 查询topic
        Result<Topic> topicResult = topicService.queryTopic(topicUserParam.getTopic());
        if(topicResult.isNotOK()) {
            logger.warn("ip:{} topic not exist:{}", ip, topicUserParam);
            return Result.getWebResult(topicResult);
        }
        Topic topic = topicResult.getResult();
        
        // 组装传输对象
        ClusterInfoDTO clusterInfoDTO = new ClusterInfoDTO();
        Cluster mqCluster = clusterService.getMQClusterById(topic.getClusterId());
        clusterInfoDTO.setClusterId(mqCluster.getId());
        clusterInfoDTO.setVipChannelEnabled(mqCluster.isEnableVipChannel());
        clusterInfoDTO.setTraceEnabled(topic.traceEnabled());
        clusterInfoDTO.setSerializer(topic.getSerializer());
        
        // 校验生产者
        if(topicUserParam.isProducer()) {
            // 查询生产者
            Result<UserProducer> userProducerResult = userProducerService.queryUserProducer(topic.getId(), topicUserParam.getGroup());
            if(userProducerResult.isNotOK()) {
                logger.warn("ip:{} user producer not exist:{}", ip, topicUserParam);
                return Result.getWebResult(userProducerResult);
            }
            saveClientVersion(topicUserParam);
            return Result.getResult(clusterInfoDTO);
        }
        
        // 查询消费者
        Result<Consumer> consumerResult = consumerService.queryTopicConsumerByName(topic.getId(), topicUserParam.getGroup());
        if(consumerResult.isNotOK()) {
            logger.warn("ip:{} user consumer not exist:{}", ip, topicUserParam);
            return Result.getWebResult(consumerResult);
        }
        Consumer consumer = consumerResult.getResult();
        clusterInfoDTO.setBroadcast(consumer.isBroadcast());
        // http方式消费的全部为广播
        if (consumer.httpConsumeEnabled()) {
            clusterInfoDTO.setBroadcast(true);
        }
        if (topic.traceEnabled()) {
            clusterInfoDTO.setTraceEnabled(consumer.traceEnabled());
        }
        saveClientVersion(topicUserParam);
        return Result.getResult(clusterInfoDTO);
    }
    
    
    /**
     * 客户端上报统计
     * 
     * @throws Exception
     */
    @RequestMapping(value = "/report", method = RequestMethod.POST)
    public Result<?> report(@RequestParam("stats") String stats) throws Exception {
        ClientStats clientStats = null;
        try {
            clientStats = JSONUtil.parse(stats, ClientStats.class);
        } catch (Exception e) {
            logger.error("json err:{}", stats, e);
        }
        if(clientStats != null) {
            boolean rst = clientStatsMemoryMQ.produce(clientStats);
            if(!rst) {
                logger.info("save failed:{}", stats);
            }
        }
        return Result.getOKResult();
    }
    
    /**
     * 保存客户端版本
     * @param topicUserParam
     */
    private void saveClientVersion(TopicUserParam topicUserParam) {
        ClientVersion cv = new ClientVersion();
        cv.setTopic(topicUserParam.getTopic());
        cv.setClient(topicUserParam.getGroup());
        cv.setRole(topicUserParam.getRole());
        if(topicUserParam.getV() == null) {
            cv.setVersion("1.8.3");
        } else {
            cv.setVersion(topicUserParam.getV());
        }
        clientVersionService.save(cv);
    }
}
