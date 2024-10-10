package com.sohu.tv.mq.cloud.service;

import com.google.common.collect.Lists;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.dao.ClusterDao;
import com.sohu.tv.mq.cloud.dao.ConsumerDao;
import com.sohu.tv.mq.cloud.dao.ConsumerTrafficDao;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
import com.sohu.tv.mq.cloud.web.controller.param.ManagerParam;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.vo.TopicStateVo;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private ClusterService clusterService;

    // 循环查询每次获取的条数
    private final static int LOOP_QUERY_LIMIT = 1000;

    // 生产者名称分隔符
    private final static String SPLIT_STR = ";";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 筛选入口
     */
    public Result<List<Topic>> queryAndBuilderTopic(ManagerParam param, PaginationParam paginationParam) {
        try {
            List<Long> tidList = new ArrayList<>();
            List<Topic> topicList = queryAndFilterTopic(param, paginationParam, tidList,false);
            if (topicList == null){
                logger.warn("query topic result is empty,the param is {}",param);
                return Result.getOKResult();
            }
            for (Topic topic : topicList) {
                topic.setCluster(clusterService.getMQClusterById(topic.getClusterId()));
            }
            return Result.getResult(topicList);
        } catch (Exception e) {
            logger.error("query topic result is err,the err message is {}",e.getCause().getMessage());
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
}
