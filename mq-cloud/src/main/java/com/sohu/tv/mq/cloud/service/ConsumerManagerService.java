package com.sohu.tv.mq.cloud.service;

import com.google.common.collect.Lists;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.ConsumerTraffic;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.dao.*;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.WebUtil;
import com.sohu.tv.mq.cloud.web.controller.param.ManagerParam;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.vo.ConsumerManagerVo;
import com.sohu.tv.mq.cloud.web.vo.ConsumerStateVo;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: 消费管理
 * @date 2022/2/28 15:05
 */
@Service
public class ConsumerManagerService extends ManagerBaseService{

    @Resource
    private ConsumerTrafficDao consumerTrafficDao;

    @Resource
    private ConsumerDao consumerDao;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 列表查询
     */
    public Result<?> queryAndFilterConsumerList(ManagerParam param, PaginationParam paginationParam) throws Exception{

        try {
            List<Long> tidList = Optional.ofNullable(param.getCid())
                    .map(c -> topicDao.selectAllTidsByCid(c)).orElse(null);
            List<Long> cidListByUid = Optional.ofNullable(param.getUid())
                    .map(u -> userConsumerDao.selectConsumerFeildListByUid(u, "consumer_id")).orElse(null);
            List<Long> cidListByGid = Optional.ofNullable(param.getGid())
                    .map(u -> userConsumerDao.selectConsumerFeildListByGid(u, "consumer_id")).orElse(null);
            Set<Long> queryCidSet = intersectionToSet(cidListByUid, cidListByGid);
            List<Consumer> consumers = consumerDao.selectByTidAndCidList(tidList, queryCidSet);
            if (CollectionUtils.isEmpty(consumers)){
                logger.warn("query consumer by topic id,the result of consumer is empty");
                return Result.getOKResult();
            }
            final List<Long> cids = consumers.stream().map(Consumer::getId).collect(Collectors.toList());
            // 消费量筛选 消费量为0或没有消费记录
            if (param.getNoneConsumerFlows() != null && param.getNoneConsumerFlows()){
                List<Long> noneFlowCids = consumerTrafficDao.selectNoneConsumerFlowsId(cids, new Date());
                consumers = consumers.stream().filter(node->
                        noneFlowCids.contains(node.getId())
                ).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(consumers)){
                return Result.getOKResult();
            }
            Map<Long, Long> consumerFlowSum = summaryConsumerFlowBy5Min(cids);
            paginationParam.caculatePagination(consumers.size());
            consumers = consumers.stream()
                    .sorted(Comparator.comparing(Consumer::getTid))
                    .skip(paginationParam.getBegin())
                    .limit(paginationParam.getNumOfPage())
                    .collect(Collectors.toList());
            List<ConsumerManagerVo> resultVo = new ArrayList<>(consumers.size());
            List<Long> queryTids = consumers.stream().map(Consumer::getTid).collect(Collectors.toList());
            List<Topic> topicList = topicDao.queryTopicDataByLimit(queryTids);
            if (CollectionUtils.isEmpty(topicList)){
                return Result.getOKResult();
            }
            Map<Long, List<Topic>> topicMap = topicList.stream().collect(Collectors.groupingBy(Topic::getId));
            int index = paginationParam.getBegin() + 1;
            for (Consumer consumer : consumers) {
                ConsumerManagerVo consumerManagerVo = new ConsumerManagerVo();
                consumerManagerVo.setConsumer(consumer);
                consumerManagerVo.setLastFiveMinusConsumerFlow(consumerFlowSum.getOrDefault(consumer.getId(),0L));
                consumerManagerVo.setTopic(Optional.ofNullable(topicMap.get(consumer.getTid())).map(node->node.get(0)).orElse(new Topic()));
                consumerManagerVo.setIndex(index ++);
                resultVo.add(consumerManagerVo);
            }
            return Result.getResult(resultVo);
        } catch (Exception e) {
            logger.error("multi condition to query consumer is err,the err message : {}",e.getMessage());
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 计算前五分钟消费流量
     */
    private Map<Long, Long> summaryConsumerFlowBy5Min(List<Long> cids) {
        Date now = new Date();
        List<String> minuteList = new ArrayList<>();
        long nowTime = System.currentTimeMillis();
        for (int i = 1; i <= 5; i++) {
            Date oneMinuteAgo = new Date(nowTime - i * 60000);
            String time = DateUtil.getFormat(DateUtil.HHMM).format(oneMinuteAgo);
            minuteList.add(time);
        }
        Map<Long, Long> consumerFlowSum = new HashMap<>();
        //查询并合并消费流量
        List<ConsumerTraffic> consumerTrafficList = consumerTrafficDao.selectFlowByDateTimeRangeAndCids(now, minuteList, cids);
        if (!CollectionUtils.isEmpty(consumerTrafficList)) {
            consumerTrafficList.forEach(node -> {
                Long sum = consumerFlowSum.getOrDefault(node.getConsumerId(), 0L);
                consumerFlowSum.put(node.getConsumerId(), sum + node.getCount());
            });
        }
        return consumerFlowSum;
    }


    /**
     * 属性查找
     */
    public Result<?> getConsumerAttribute(long cid) {
        try {
            Consumer consumer = consumerDao.selectById(cid);
            if (consumer == null) {
                return Result.getResult(-1);
            }
            return Result.getResult(consumer.getConsumeWay());
        } catch (Exception e) {
            logger.error("getConsumerAttribute is err,the err message : {}",e.getMessage());
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 更改消费属性
     */
    public Result<?> editConsumerType(long cid, int consumeWay, HttpServletRequest request){
        try {
            UserInfo userInfo = (UserInfo) WebUtil.getAttribute(request, UserInfo.USER_INFO);
            consumerDao.updateConsumerWay(cid, consumeWay);
            logger.warn("the consumer consume_way is update,the consumer id is {},the update consumerWay is {},the operator id is {}, " +
                    "the update time is {}",cid,consumeWay,userInfo.getUser().getEmail(), DateUtil.formatYMD(new Date()));
            return Result.getOKResult();
        } catch (Exception e) {
            logger.error("editConsumerType is err,the err message : {}",e.getMessage());
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 当前消费者状态
     */
    public Result<?> getConsumerState(long cid, long tid) {
        try {
            // 时间范围
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime startLocalTime = LocalDateTime.now();
            LocalDateTime endLocalTime = startLocalTime.minusDays(30);
            Date endTime = Date.from(startLocalTime.atZone(zoneId).toInstant());
            Date startTime = Date.from(endLocalTime.atZone(zoneId).toInstant());
            Long flowsCount = consumerTrafficDao.selectSummaryDataByRangeTime(Lists.newArrayList(cid), startTime, endTime);
            int cSize = consumerDao.selectByTid(tid).size();
            ConsumerStateVo consumerStateVo = new ConsumerStateVo();
            consumerStateVo.setRecentMonConMsgNum(flowsCount == null? 0L:flowsCount);
            consumerStateVo.setOnlyRelation(cSize==1?0:1);
            return Result.getResult(consumerStateVo);
        } catch (Exception e) {
            logger.error("getConsumerState is err,the err message : {}",e.getMessage());
            return Result.getDBErrorResult(e);
        }
    }
}
