package com.sohu.tv.mq.cloud.service;

import com.google.common.collect.Lists;
import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.dao.*;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.util.WebUtil;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.controller.param.ManagerParam;
import com.sohu.tv.mq.cloud.web.vo.TopicManagerInfoVo;
import com.sohu.tv.mq.cloud.web.vo.TopicStateVo;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: TODO
 * @date 2022/2/13 19:38
 */
@Service
public class TopicManagerService extends ManagerBaseService{

    @Resource
    private ConsumerTrafficDao consumerTrafficDao;

    @Resource
    private ClusterDao clusterDao;

    @Resource
    private ConsumerDao consumerDao;

    // 循环查询每次获取的条数
    private final static int LOOP_QUERY_LIMIT = 1000;

    // 生产者名称分隔符
    private final static String SPLIT_STR = ";";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 筛选入口
     */
    public Result<List<TopicManagerInfoVo>> queryAndBuilderTopic(ManagerParam param, PaginationParam paginationParam) {

        try {
            List<Long> tidList = new ArrayList<>();
            List<Topic> topicList = comonFilterTopic(param, paginationParam, tidList,false);
            if (topicList == null){
                logger.warn("query topic result is empty,the param is {}",param);
                return Result.getOKResult();
            }

            //进行时间段查询
            Map<Date, List<String>> dateRange = calculationDateRange();
            Map<Long, Long> producerFlowSum = new HashMap<>(10);
            Map<Long, Long> consumerFlowSum = new HashMap<>(10);

            dateRange.forEach((key, value) -> {
                //查询并合并生产流量
                List<TopicTraffic> producerListTraffic = topicTrafficDao.selectByDateTimeRange(key, value, tidList);
                if (!CollectionUtils.isEmpty(producerListTraffic)) {
                    producerListTraffic.forEach(node -> {
                        Long sum = producerFlowSum.getOrDefault(node.getTid(), 0L);
                        producerFlowSum.put(node.getTid(), sum + node.getCount());
                    });
                }
                //查询并合并消费流量
                List<ConsumerTraffic> consumerListTraffic = consumerTrafficDao.selectFlowByDateTimeRange(key, value, tidList);
                if (!CollectionUtils.isEmpty(consumerListTraffic)) {
                    consumerListTraffic.forEach(node -> {
                        Long sum = consumerFlowSum.getOrDefault(node.getConsumerId(), 0L);
                        // 获取consumerID也就Tid,做了映射
                        consumerFlowSum.put(node.getConsumerId(), sum + node.getCount());
                    });
                }
            });

            //封装topic集群信息
            List<Long> cids = topicList.stream().map(Topic::getClusterId).distinct().collect(Collectors.toList());
            List<Cluster> clusters = clusterDao.selectClusterByCids(cids);

            //组装返回信息
            return builderNormalManagerInfo(topicList, producerFlowSum, consumerFlowSum, clusters, paginationParam);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("query topic result is err,the err message is {}",e.getCause().getMessage());
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 增加生产者
     */
    public Result<?> addProducers(Long tid, String pNames, Long userId,HttpServletRequest request) {
        try {
            String[] productNameList = pNames.split(SPLIT_STR);
            List<UserProducer> list = new ArrayList<>(productNameList.length);
            //校验相同生产者是否存在
            String existName = userProducerDao.checkExistByName(Lists.newArrayList(productNameList));
            if (existName != null){
                return Result.getResult(Status.DB_DUPLICATE_KEY).setMessage(existName + "已存在");
            }
            for (String name : productNameList) {
                UserProducer userProducer = new UserProducer();
                userProducer.setProducer(name);
                userProducer.setUid(userId);
                userProducer.setTid(tid);
                list.add(userProducer);
            }
            if (CollectionUtils.isEmpty(list)) {
                return Result.getOKResult();
            }
            Integer batchInsertCount = userProducerDao.batchInsert(list);
            UserInfo userInfo = (UserInfo) WebUtil.getAttribute(request, UserInfo.USER_INFO);
            logger.warn("add producer for topic,the topic id is {},producer names is {},the operator is {},the date is {}",
                    tid,pNames,userInfo.getUser().getEmail(),DateUtil.formatYMD(new Date()));
            return Result.getResult(batchInsertCount);
        } catch (Exception e) {
            logger.error("addProducers is err,the err message is {}",e.getMessage());
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 获取指定Topic状态
     */
    public Result<?> getTopicState(Long tid) {
        try {
            // 当前关联的消费者数量
            List<Topic> topicList = topicDao.selectByIdList(Lists.newArrayList(tid));
            if (CollectionUtils.isEmpty(topicList)) {
                return Result.getOKResult();
            }
            Consumer consumer = new Consumer();
            consumer.setTid(tid);
            List<Long> consumerList = consumerDao.selectByTidList(Lists.newArrayList(tid)).stream().map(Consumer::getId).collect(Collectors.toList());
            int cSize = consumerList.size();
            // 时间范围
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime startLocalTime = LocalDateTime.now();
            LocalDateTime endLocalTime = startLocalTime.minusDays(30);
            Date endTime = Date.from(startLocalTime.atZone(zoneId).toInstant());
            Date startTime = Date.from(endLocalTime.atZone(zoneId).toInstant());
            // 近30天生产量
            Long producerNum = topicTrafficDao.selectSummaryDataByRangeTime(tid, startTime, endTime);
            // 近30天消费量
            Long consumerNum = 0L;
            if (cSize != 0) {
                consumerNum = Optional.ofNullable(consumerTrafficDao.selectSummaryDataByRangeTime(consumerList, startTime, endTime))
                        .orElse(0L);
            }
            TopicStateVo vo = new TopicStateVo();
            vo.setRelationConsumers(cSize);
            vo.setRecentMonConMsgNum(consumerNum);
            vo.setRecentMonProMsgNum(producerNum == null? 0L: producerNum);
            vo.setCreateTime(DateUtil.getFormat(DateUtil.YMD_DASH).format(topicList.get(0).getCreateDate()));
            return Result.getResult(vo);
        } catch (Exception e) {
            logger.error("getTopicState is err,the err message is {}",e.getMessage());
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 状态确认
     */
    public Result<?> confirmStatus(long tid, HttpServletRequest request) {
        try {
            topicDao.updateCheckStatus(tid);
            UserInfo userInfo = (UserInfo) WebUtil.getAttribute(request, UserInfo.USER_INFO);
            logger.warn("confirm topic status,the tid is {},the operator is {},the date is {}",tid,
                    userInfo.getUser().getEmail(),DateUtil.formatYMD(new Date()));
            return Result.getOKResult();
        } catch (Exception e) {
            logger.error("confirmStatus is err,the err message is {}",e.getMessage());
            return Result.getDBErrorResult(e);
        }
    }



    /**
     * 构建topic封装对象
     */
    private Result<List<TopicManagerInfoVo>> builderNormalManagerInfo(List<Topic> topicList, Map<Long, Long> producerFlowSum, Map<Long, Long> consumerFlowSum, List<Cluster> clusters, PaginationParam paginationParam) {
        Map<Long, List<Cluster>> clusterGroup = clusters.stream().collect(Collectors.groupingBy(key -> Long.valueOf(key.getId())));
        List<TopicManagerInfoVo> resultList = new ArrayList<>();
        int index = paginationParam.getBegin();
        for (Topic topic : topicList) {
            topic.setCluster(clusterGroup.get(topic.getClusterId()).get(0));
            TopicManagerInfoVo vo = null;
            if (producerFlowSum == null || consumerFlowSum == null) {
                vo = new TopicManagerInfoVo(++index, topic, 0L, 0L);
            } else {
                vo = new TopicManagerInfoVo(++index, topic, producerFlowSum.getOrDefault(topic.getId(), 0L), consumerFlowSum.getOrDefault(topic.getId(), 0L));
            }
            resultList.add(vo);
        }
        return Result.getResult(resultList);
    }




}
