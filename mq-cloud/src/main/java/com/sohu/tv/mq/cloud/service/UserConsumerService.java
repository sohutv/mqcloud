package com.sohu.tv.mq.cloud.service;

import java.util.List;
import java.util.Set;

import org.apache.rocketmq.common.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserConsumer;
import com.sohu.tv.mq.cloud.dao.UserConsumerDao;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
/**
 * 用户消费服务
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月12日
 */
@Service
public class UserConsumerService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private UserConsumerDao userConsumerDao;
    
    @Autowired
    private ConsumerService consumerService;
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    /**
     * 插入用户记录
     * @param user
     * @return 返回Result
     */
    @Transactional
    public Result<?> saveUserConsumer(Cluster cluster, UserConsumer userConsumer, Consumer consumer) {
        try {
            // 第一步：创建消费者记录
            Integer count = consumerService.save(consumer);
            // 第二步：创建用户消费者关联关系
            if(count != null && count > 0 && consumer.getId() > 0) {
                userConsumer.setConsumerId(consumer.getId());
                userConsumerDao.insert(userConsumer);
            }
            // 第三步：真实创建消费者
            Result<?> result = createAndUpdateConsumerOnCluster(cluster, consumer);
            if(result.isNotOK()) {
                throw new RuntimeException("create consumer:"+consumer.getName()+" on cluster err!");
            }
        } catch (Exception e) {
            logger.error("insert err, userConsumer:{}", userConsumer, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 创建consumer
     * @param mqCluster
     * @param consumer
     */
    public Result<?> createAndUpdateConsumerOnCluster(Cluster mqCluster, Consumer consumer) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                long start = System.currentTimeMillis();
                Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(mqAdmin, mqCluster.getName());
                SubscriptionGroupConfig subscriptionGroupConfig = new SubscriptionGroupConfig();
                subscriptionGroupConfig.setGroupName(consumer.getName());
                for (String addr : masterSet) {
                    mqAdmin.createAndUpdateSubscriptionGroupConfig(addr, subscriptionGroupConfig);
                }
                long end = System.currentTimeMillis();
                logger.info("create or update consumer use:{}ms, consumer:{}", (end- start), 
                        subscriptionGroupConfig.getGroupName());
                return Result.getOKResult();
            }
            @Override
            public Result<?> exception(Exception e) throws Exception {
                logger.error("create or update consumer:{} err:{}", consumer.getName(), e.getMessage());
                return Result.getWebErrorResult(e);
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }
    
    /**
     * 插入用户记录
     * @param user
     * @return 返回Result
     */
    public Result<?> saveNoException(UserConsumer userConsumer) {
        try {
            userConsumerDao.insert(userConsumer);
        } catch (Exception e) {
            logger.error("insert err, userConsumer:{}", userConsumer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 查询用户消费者关系
     * @param userConsumer
     * @return
     */
    public Result<List<UserConsumer>> queryUserConsumer(UserConsumer userConsumer){
        List<UserConsumer> userConsumerList = null;
        try {
            userConsumerList = userConsumerDao.select(userConsumer);
        } catch (Exception e) {
            logger.error("select err, userConsumer:{}", userConsumer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userConsumerList);
    }
    
    /**
     * 查询用户消费者关系
     * @param userConsumer
     * @return
     */
    public Result<List<UserConsumer>> queryUserConsumerByConsumerId(long cid){
        UserConsumer userConsumer = new UserConsumer();
        userConsumer.setConsumerId(cid);
        return queryUserConsumer(userConsumer);
    }
    
    /**
     * 查询用户消费者关系(包含权限)
     * @param user
     * @param tid
     * @param cid
     * @return
     */
    public Result<List<UserConsumer>> queryUserConsumer(long tid){
        UserConsumer userConsumer = new UserConsumer();
        userConsumer.setTid(tid);
        return queryUserConsumer(userConsumer);
    }
    
    /**
     * 查询用户消费者关系
     * @param uid
     * @param consumerID
     * @return
     */
    public Result<List<UserConsumer>> queryUserConsumer(long uid, long consumerID){
        UserConsumer userConsumer = new UserConsumer();
        userConsumer.setConsumerId(consumerID);
        userConsumer.setUid(uid);
        return queryUserConsumer(userConsumer);
    }
    /**
     * 查询用户消费者关系(包含权限)
     * @param user
     * @param tid
     * @param cid
     * @return
     */
    public Result<List<UserConsumer>> queryUserConsumer(User user, long tid, long cid){
        UserConsumer userConsumer = new UserConsumer();
        userConsumer.setTid(tid);
        userConsumer.setConsumerId(cid);
        if(!user.isAdmin()) {
            userConsumer.setUid(user.getId());
        }
        return queryUserConsumer(userConsumer);
    }
    
    /**
     * 查询topic的消费者
     * @param userConsumer
     * @return
     */
    public Result<List<UserConsumer>> queryTopicConsumer(long tid, List<Long> cidList){
        List<UserConsumer> userConsumerList = null;
        try {
            userConsumerList = userConsumerDao.selectByCidList(tid, cidList);
        } catch (Exception e) {
            logger.error("queryTopicConsumer err, tid:{},cidList:{}", tid, cidList, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userConsumerList);
    }
    
    /**
     * 查询消费者
     * @param userConsumer
     * @return
     */
    public Result<List<UserConsumer>> queryByNameAndTid(long tid, String consumerName){
        List<UserConsumer> userConsumerList = null;
        try {
            userConsumerList = userConsumerDao.selectByNameAndTid(consumerName, tid);
        } catch (Exception e) {
            logger.error("queryByNameAndTid err, tid:{},consumerName:{}", tid, consumerName, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userConsumerList);
    }
  
    /**
     * 查询用户
     * @param userConsumer
     * @return
     */
    public Result<List<User>> queryUserByConsumer(long tid, long cid){
        List<User> userList = null;
        try {
            userList = userConsumerDao.selectUserByConsumer(tid, cid);
        } catch (Exception e) {
            logger.error("user err, tid:{},cid:{}", tid, cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userList);
    }
    
    /**
     * 查询用户消费者关系
     * @param id
     * @return
     */
    public Result<UserConsumer> selectById(long id){
        UserConsumer userConsumer = null;
        try {
            userConsumer = userConsumerDao.selectById(id);
        } catch (Exception e) {
            logger.error("selectById err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userConsumer);
    }
    
    /**
     * 查询用户消费者关系
     * @param id
     * @return
     */
    public Result<List<UserConsumer>> selectByConsumerId(long id){
        List<UserConsumer> userConsumerList = null;
        try {
            userConsumerList = userConsumerDao.selectByConsumerId(id);
        } catch (Exception e) {
            logger.error("selectByConsumerId err, ConsumerId:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userConsumerList);
    }
    
    /**
     * 删除消费者
     * @param id
     * @return
     */
    public Result<Integer> deleteById(long id){
        Integer count = null;
        try {
            count = userConsumerDao.deleteById(id);
            if(count == null || count != 1) {
                return Result.getResult(Status.DB_ERROR);
            }
        } catch (Exception e) {
            logger.error("selectById err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }
}
